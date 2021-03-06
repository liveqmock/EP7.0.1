package com.elasticpath.repo.camelimport.processors;

import java.util.Collection;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.repo.camelimport.CamelImportConstants;
import com.elasticpath.service.changeset.ChangeSetLoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.service.changeset.ChangeSetSearchCriteria;
import com.elasticpath.service.cmuser.CmUserService;

/**
 * Process a set of files into a changeset.
 */
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public class CreateChangesetProcessor implements Processor {

	private BeanFactory beanFactory;

	private static final String CM_USER_NAME = "Admin";

	public static String getCmUserName() {
		return CM_USER_NAME;
	}

	/**
	 * Create the change set for the dest dir received on the exchange.
	 * 
	 * @param exchange in coming exchange
	 * @throws Exception Unrecoverable exception if dest dir not found on the exchange.
	 */
	@Override	
	public void process(final Exchange exchange) throws Exception {
		// get the destDir from the exchange
		String destDir = (String) exchange.getProperty(CamelImportConstants.IMPORT_DATA_DIR_PATH);
		if (StringUtils.isBlank(destDir)) {
			throw new ConfigurationException("Dest-dir was not found on the exchange");
		}

		// get SYSTEM CMUser
		CmUserService cmUserService = beanFactory.getBean(ContextIdNames.CMUSER_SERVICE);
		CmUser cmUser = cmUserService.findByUserName(CM_USER_NAME);
		if (cmUser == null) {
			throw new RuntimeException(CM_USER_NAME + " CM User was not found");
		}

		String changeSetName = "Generated By Catalog Feed -- " + exchange.getIn().getHeader(Exchange.FILE_NAME_ONLY);
		// see if the changeset already exists
		ChangeSetSearchCriteria criteria = new ChangeSetSearchCriteria();
		criteria.setChangeSetName(changeSetName);
		criteria.setStrictName(true);
		criteria.setUserGuid(cmUser.getGuid());

		ChangeSet newChangeSet;
		ChangeSetLoadTuner changeSetLoadTuner = beanFactory.getBean(ContextIdNames.CHANGESET_LOAD_TUNER);
		changeSetLoadTuner.setLoadingMemberObjects(false);
		changeSetLoadTuner.setLoadingMemberObjectsMetadata(false);

		ChangeSetManagementService changeSetManagementService = beanFactory.getBean(ContextIdNames.CHANGESET_MANAGEMENT_SERVICE);
		Collection<ChangeSet> foundChangeSets = changeSetManagementService.findByCriteria(criteria, changeSetLoadTuner);
		if (CollectionUtils.size(foundChangeSets) > 1) {
			throw new EpSystemException("More than one change set was found.");
		} else if (CollectionUtils.isEmpty(foundChangeSets)) {
			// create a new change set and load it with required info
			ChangeSet changeSet = beanFactory.getBean(ContextIdNames.CHANGE_SET);
			changeSet.setName(changeSetName);
			changeSet.setCreatedDate(new Date());
			changeSet.setStateCode(ChangeSetStateCode.OPEN);
			changeSet.setCreatedByUserGuid(cmUser.getGuid());

			// generate a new changeSet and get the guid
			newChangeSet = changeSetManagementService.add(changeSet);
		} else {
			newChangeSet = foundChangeSets.iterator().next();
			if (!ChangeSetStateCode.OPEN.equals(newChangeSet.getStateCode())) {
				// We found a matching changeset, but it is not OPEN, so we cannot safely reuse it.
				throw new EpSystemException("Cannot reuse the changeset with name [" + changeSetName + "] because it is not OPEN, it is ["
						+ newChangeSet.getStateCode() + "].");
			}
		}

		String changeSetGUID = newChangeSet.getGuid();

		// put the guid on the exchange
		exchange.setProperty(CamelImportConstants.CHANGESET_GUID, changeSetGUID);
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
