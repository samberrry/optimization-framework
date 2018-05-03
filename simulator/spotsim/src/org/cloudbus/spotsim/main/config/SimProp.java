package org.cloudbus.spotsim.main.config;

import java.util.GregorianCalendar;

public interface SimProp {

    boolean asBoolean();

    GregorianCalendar asDate();

    double asDouble();

    Enum<?> asEnum();

    <E extends Enum<E>> E asEnum(Class<E> type);

    int asInt();

    long asLong();

    Object asObject();

    String asString();

    void set(Object value);

    void read(String property);

    <T> T asType(Class<T> clazz);

    boolean isNumber();

    String numberAsString(String pattern);
}
