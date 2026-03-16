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

public class German extends antafes.utilities.language.German
{
    public German()
    {
        super();
        this.general();
        this.newRefinement();
    }

    private void general()
    {
        this.getTranslations().put("title", "SC Raffinerie-Manager");
        this.getTranslations().put("refinements", "Raffinationen");
        this.getTranslations().put("revenue", "Umsatz");
        this.getTranslations().put("profit", "Gewinn");
        this.getTranslations().put("materials", "Materialien");
        this.getTranslations().put("actions", "Aktionen");
        this.getTranslations().put("deleteRefinementTitle", "Raffination löschen");
        this.getTranslations().put("deleteRefinementConfirm", "Möchtest du Raffination #%d wirklich löschen?");
        this.getTranslations().put("editRefinement", "Raffination #%d bearbeiten");
        this.getTranslations().put("createdAt", "Erstellt am");
    }

    private void newRefinement()
    {
        this.getTranslations().put("newRefinement", "Neue Raffination erstellen");
        this.getTranslations().put("cost", "Kosten");
        this.getTranslations().put("addMaterialRow", "Material hinzufügen");
        this.getTranslations().put("material", "Material");
        this.getTranslations().put("amount", "Menge");
        this.getTranslations().put("quality", "Qualität");
        this.getTranslations().put("costRequired", "Kosten sind erforderlich.");
        this.getTranslations().put("materialRequired", "Material ist erforderlich.");
        this.getTranslations().put("amountRequired", "Menge ist erforderlich.");
        this.getTranslations().put("revenueRequired", "Umsatz ist erforderlich.");
    }
}
