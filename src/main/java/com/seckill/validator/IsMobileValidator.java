package com.seckill.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.seckill.util.ValidatorUtil;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String>{
	private boolean required;
	
	@Override
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		//判断值是否必须项，返回验证结果，如果不是则再判断是否为空
		if(required) {
			return ValidatorUtil.isMobile(value);
		}
		else {
			if(StringUtils.isEmpty(value)) {
				return true;
			}
			else {
				return ValidatorUtil.isMobile(value);
			}
		}
		
	}

}
