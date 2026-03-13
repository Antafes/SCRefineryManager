/*
 * This file is part of SCRefineryManager.
 *
 * SCRefineryManager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SCRefineryManager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SCRefineryManager. If not, see <http://www.gnu.org/licenses/>.
 *
 * @package SCRefineryManager
 * @author Marian Pollzien <map@wafriv.de>
 * @copyright (c) 2026, Marian Pollzien
 * @license https://www.gnu.org/licenses/lgpl.html LGPLv3
 */

package antafes.sc.refinery.manager.gui.event;

import scripts.laniax.framework.event_dispatcher.EventListener;

import java.util.function.Consumer;

public class RegisterEscapeCloseOperationListener extends EventListener<RegisterEscapeCloseOperationEvent>
{
    public RegisterEscapeCloseOperationListener()
    {
    }

    public RegisterEscapeCloseOperationListener(Consumer<RegisterEscapeCloseOperationEvent> consumer)
    {
        super(consumer);
    }

    public RegisterEscapeCloseOperationListener(
        Consumer<RegisterEscapeCloseOperationEvent> consumer, int priority
    )
    {
        super(consumer, priority);
    }
}
