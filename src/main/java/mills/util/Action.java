package mills.util;

/**
* Created by IntelliJ IDEA.
* User: stueken
* Date: 20.05.13
* Time: 17:20
*/
public interface Action<T> {

    /**
     * Execute some action on actor.
     * @param actor to act on.
     */
    void act(T actor);

    @Override
    String toString();
}
