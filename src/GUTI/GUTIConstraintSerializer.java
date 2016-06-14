package GUTI;

import java.lang.reflect.Constructor;

import checkers.inference.model.Serializer;
import constraintsolver.ConstraintSerializer;

public class GUTIConstraintSerializer<S, T> extends ConstraintSerializer<S, T> {


    @SuppressWarnings("unchecked")
    public GUTIConstraintSerializer(String backEndType) {
        super(backEndType);
        try {
            String refinedBackEndType = backEndType;
            if (backEndType.contains("MaxSat")) {
                refinedBackEndType = backEndType.replace("maxsatbackend.", "");
            }
            Class<?> classObjectOfRealSerializer = Class.forName("GUTI.GUTI" + refinedBackEndType + "Serializer");
            Constructor<T> constructor = (Constructor<T>) classObjectOfRealSerializer
                    .getConstructor();
            realSerializer = (Serializer<S, T>) constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
