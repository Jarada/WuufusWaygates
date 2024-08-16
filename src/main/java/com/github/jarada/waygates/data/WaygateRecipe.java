package com.github.jarada.waygates.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;


public class WaygateRecipe {
    private List<String> _recipeList;
    private Material _itemIcon;
    private Set<Character> _ingredients = new HashSet<Character>(9);

    public WaygateRecipe(List<String> recipeList, Material itemIcon) {
        this._recipeList = recipeList;
        this._itemIcon = itemIcon;
    }

    public WaygateRecipe(List<String> recipeList, String itemIconName) {
        this._recipeList = recipeList;
        this._itemIcon = Material.matchMaterial(itemIconName);
    }

    public List<String> getRecipeList() {
        return _recipeList;
    }

    public Material getItemIcon() {
        return _itemIcon;
    }

    public int rowCount() {
        return _recipeList.size();
    }

    public String getRecipeRow(int row) {
        if (row < 0 || row > _recipeList.size()) {
            throw new IllegalArgumentException("Index must be greater than zero and less than the number of recipe rows minus one.");
        }
        if (_recipeList.size() > row) {
            return _recipeList.get(row);
        } else {
            throw new IndexOutOfBoundsException("No element at index " + row);
        }
    }

    public Set<Character> getIngredientKeys() {
        if (_ingredients.size() == 0) {
            for (String row : _recipeList) {
                for (Character key : row.toCharArray()) {
                    _ingredients.add(key);
                }
            }
        }
        return _ingredients;
    }
}
