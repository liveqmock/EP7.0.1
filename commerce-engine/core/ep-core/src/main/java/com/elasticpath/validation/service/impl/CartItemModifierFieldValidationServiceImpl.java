/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.validation.service.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;

import com.google.common.collect.Maps;

import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.cartmodifier.CartItemModifierField;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.service.CartItemModifierFieldValidationService;
import com.elasticpath.validation.validators.util.DynamicCartItemModifierField;
import com.elasticpath.validation.validators.util.DynamicCartItemModifierFieldValidator;

/**
 * Service for dynamic validation of {@link CartItemModifierField} fields.
 */
public class CartItemModifierFieldValidationServiceImpl implements CartItemModifierFieldValidationService {

	/*
	 * Flag indicates required suppression or not.
	 */
	private boolean isRequiredSuppression;

	private ConstraintViolationTransformer constraintViolationTransformer;

	public void setConstraintViolationTransformer(final ConstraintViolationTransformer constraintViolationTransformer) {
		this.constraintViolationTransformer = constraintViolationTransformer;
	}

	@Override
	public List<StructuredErrorMessage> validate(final Map<String, String> itemsToValidate,
			final Set<CartItemModifierField> referentFields) {

		final Map<String, CartItemModifierField> refFieldNameToField = Maps.uniqueIndex(referentFields, CartItemModifierField::getCode);

		if (refFieldNameToField.isEmpty()) {
			return Collections.emptyList();
		}

		final Set<ConstraintViolation<DynamicCartItemModifierField>> violations = new LinkedHashSet<>();
		for (Map.Entry<String, String> dynamicPropertyToValidate : itemsToValidate.entrySet()) {
			final String propertyNameBeingValidated = dynamicPropertyToValidate.getKey();
			final String propertyValueToValidate = dynamicPropertyToValidate.getValue();

			final CartItemModifierField referentField = refFieldNameToField.get(propertyNameBeingValidated);

			final DynamicCartItemModifierField dynamicCartItemModifierField = new DynamicCartItemModifierField(propertyNameBeingValidated,
					propertyValueToValidate, referentField);

			violations.addAll(new DynamicCartItemModifierFieldValidator(isRequiredSuppression)
					.validate(dynamicCartItemModifierField));
		}

		return constraintViolationTransformer.transform(violations);
	}

	public void setRequiredSuppression(final boolean requiredSuppression) {
		isRequiredSuppression = requiredSuppression;
	}
}
