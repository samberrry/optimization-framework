package org.cloudbus.spotsim.enums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

/**
 * To be used a map key, or to make permutations
 */
public class EnumBundle4<E1 extends Enum<E1>, E2 extends Enum<E2>, E3 extends Enum<E3>, E4 extends Enum<E4>> {

    private final E1 enum1;

    private final E2 enum2;

    private final E3 enum3;

    private final E4 enum4;

    public EnumBundle4(E1 enum1, E2 enum2, E3 enum3, E4 enum4) {
	super();
	this.enum1 = enum1;
	this.enum2 = enum2;
	this.enum3 = enum3;
	this.enum4 = enum4;
    }

    public E1 getEnum1() {
	return this.enum1;
    }

    public E2 getEnum2() {
	return this.enum2;
    }

    public E3 getEnum3() {
	return this.enum3;
    }

    public E4 getEnum4() {
	return this.enum4;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + this.enum1.hashCode();
	result = prime * result + this.enum2.hashCode();
	result = prime * result + this.enum3.hashCode();
	result = prime * result + this.enum4.hashCode();
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	EnumBundle4 other = (EnumBundle4) obj;
	if (!this.enum1.equals(other.enum1)) {
	    return false;
	}
	if (!this.enum2.equals(other.enum2)) {
	    return false;
	}
	if (!this.enum3.equals(other.enum3)) {
	    return false;
	}
	if (!this.enum4.equals(other.enum4)) {
	    return false;
	}
	return true;
    }

    public static <E1 extends Enum<E1>, E2 extends Enum<E2>, E3 extends Enum<E3>, E4 extends Enum<E4>> Collection<EnumBundle4<E1, E2, E3, E4>> permute(
	    Class<E1> c1, Class<E2> c2, Class<E3> c3, Class<E4> c4) {
	Collection<EnumBundle4<E1, E2, E3, E4>> ret = new ArrayList<EnumBundle4<E1, E2, E3, E4>>();

	new EnumBundle4<E1, E2, E3, E4>(null, null, null, null);

	EnumSet<E1> c11 = EnumSet.allOf(c1);
	EnumSet<E2> c22 = EnumSet.allOf(c2);
	EnumSet<E3> c33 = EnumSet.allOf(c3);
	EnumSet<E4> c44 = EnumSet.allOf(c4);

	for (E1 enum1 : c11) {
	    for (E2 enum2 : c22) {
		for (E3 enum3 : c33) {
		    for (E4 enum4 : c44) {
			ret.add(new EnumBundle4<E1, E2, E3, E4>(enum1, enum2, enum3, enum4));
		    }
		}
	    }
	}

	return ret;
    }
}