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
 */

package antafes.sc.refinery.manager.gui.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import scripts.laniax.framework.event_dispatcher.Event;

import javax.swing.JDialog;

@Getter
@RequiredArgsConstructor
public class SaveEditRefinementEvent extends Event
{
    private final @NonNull JDialog dialog;
    private final @NonNull Integer key;
}
