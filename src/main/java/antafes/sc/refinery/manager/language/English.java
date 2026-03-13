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

package antafes.sc.refinery.manager.language;

public class English extends antafes.utilities.language.English
{
    public English()
    {
        super();
        this.general();
        this.newRefinement();
    }

    private void general()
    {
        this.getTranslations().put("title", "SC Refinery Manager");
        this.getTranslations().put("refinements", "Refinements");
        this.getTranslations().put("revenue", "Revenue");
        this.getTranslations().put("profit", "Profit");
        this.getTranslations().put("actions", "Actions");
        this.getTranslations().put("deleteRefinementTitle", "Delete refinement");
        this.getTranslations().put("deleteRefinementConfirm", "Do you really want to delete refinement #%d?");
    }

    private void newRefinement()
    {
        this.getTranslations().put("newRefinement", "Create new refinement");
        this.getTranslations().put("cost", "Cost");
        this.getTranslations().put("addMaterialRow", "Add material");
        this.getTranslations().put("material", "Material");
        this.getTranslations().put("amount", "Amount");
        this.getTranslations().put("quality", "Quality");
        this.getTranslations().put("costRequired", "Cost is required.");
        this.getTranslations().put("materialRequired", "Material is required.");
        this.getTranslations().put("amountRequired", "Amount is required.");
    }
}
