package antafes.sc.refinery.manager.gui.event;

import scripts.laniax.framework.event_dispatcher.EventListener;

import java.util.function.Consumer;

public class NewRefinementListener extends EventListener<NewRefinementEvent>
{
    public NewRefinementListener()
    {
    }

    public NewRefinementListener(Consumer<NewRefinementEvent> consumer)
    {
        super(consumer);
    }

    public NewRefinementListener(Consumer<NewRefinementEvent> consumer, int priority)
    {
        super(consumer, priority);
    }
}
