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

	public void setSerialVersionUID(long serialVersionUID) {
		this.serialVersionUID = serialVersionUID;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getColor() {
		return color;
	}

	public float getWeight() {
		return weight;
	}
}
