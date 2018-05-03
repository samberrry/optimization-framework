package org.cloudbus.cloudsim.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StreamCloner {

    @SuppressWarnings("unchecked")
    public static <T> T clone(final T objectToBeCloned) throws CloneNotSupportedException {

	ObjectOutputStream objectOOS = null;
	ObjectInputStream objectOIS = null;

	try {
	    final ByteArrayOutputStream objectBAOS = new ByteArrayOutputStream();
	    objectOOS = new ObjectOutputStream(objectBAOS);
	    objectOOS.writeObject(objectToBeCloned);

	    final ByteArrayInputStream objectBAIS = new ByteArrayInputStream(
		objectBAOS.toByteArray());
	    objectOIS = new ObjectInputStream(objectBAIS);
	    return (T) objectOIS.readObject();

	} catch (final NotSerializableException e) {
	    throw new CloneNotSupportedException("Class " + e.getMessage() + "is not serializable");
	} catch (final IOException e) {
	    throw new CloneNotSupportedException("The given object could not be cloned."
		    + e.getMessage());
	} catch (final ClassNotFoundException e) {
	    throw new CloneNotSupportedException("The given object could not be cloned."
		    + e.getMessage());
	} finally {

	    if (objectOOS != null) {
		try {
		    objectOOS.close();
		} catch (final IOException e1) {
		}
	    }

	    if (objectOIS != null) {
		try {
		    objectOIS.close();
		} catch (final IOException e1) {
		}
	    }
	}
    }
}