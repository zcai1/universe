package GUTI;

import java.lang.reflect.Constructor;

import javax.lang.model.util.Elements;

import checkers.inference.model.Serializer;
import constraintsolver.ConstraintSerializer;

public class GUTIConstraintSerializer<S, T> extends ConstraintSerializer<S, T> {


    @SuppressWarnings("unchecked")
    public GUTIConstraintSerializer(String backEndType,
            Elements elements) {
        super(backEndType);
        try {
            if (backEndType.contains("MaxSat")) {
                backEndType.replace("maxsatbackend.", "");
            }
            Class<?> classObjectOfRealSerializer = Class
                    .forName("GUTI" + backEndType + "Serializer");
            Constructor<T> constructor = (Constructor<T>) classObjectOfRealSerializer
                    .getConstructor(Elements.class);
            realSerializer = (Serializer<S, T>) constructor
                    .newInstance(elements);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
