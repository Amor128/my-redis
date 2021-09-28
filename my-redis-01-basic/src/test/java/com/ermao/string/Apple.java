package com.ermao.string;

import java.io.Serializable;

/**
 * @author Ermao
 * Date: 2021/9/29 0:00
 */
public class Apple implements Serializable {
	long serialVersionUID = 1L;
	String color = "red";
	float weight = 3.14f;

	@Override
	public boolean equals(Object obj) {
		Apple target = obj instanceof Apple ? ((Apple) obj) : null;
		assert target != null;
		return this.color.equals(target.color)
				&& this.weight == target.weight
				&& this.serialVersionUID == target.serialVersionUID;
	}
}
