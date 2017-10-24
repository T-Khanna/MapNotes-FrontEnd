package mapnotes.mapnotes.data_classes;

import java.io.Serializable;

/**
 * An abstract function class, that can be used for callbacks
 * Only one method, run, that will take one input (the response
 * from the server)
*/
public abstract class Function <T> implements Serializable {
    public abstract void run(T input);
}
