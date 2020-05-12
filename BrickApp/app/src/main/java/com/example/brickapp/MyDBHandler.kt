package com.example.brickapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.w3c.dom.NodeList

class MyDBHandler (context: Context, name: String?,
                   factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "BrickList.db"

        val TABLE_INVENTORIES = "Inventories"
        val COLUMN_ID = "id"
        val COLUMN_NAME = "Name"
        val COLUMN_ACTIVE = "Active"
        val COLUMN_LAST_ACCESSED = "LastAccessed"

        val TABLE_INVENTORIES_PARTS = "InventoriesParts"
        val COLUMN_INVENTORY_ID = "InventoryID"
        val COLUMN_TYPE_ID = "TypeID"
        val COLUMN_ITEM_ID = "ItemID"
        val COLUMN_QUANTITY_IN_SET = "QuantityInSet"
        val COLUMN_QUANTITY_IN_STORE = "QuantityInStore"
        val COLUMN_COLOR_ID = "ColorID"

        val TABLE_ITEM_TYPES = "ItemTypes"

        val TABLE_CODES = "Codes"
        val COLUMN_IMAGE = "Image"

        val TABLE_PARTS = "Parts"

        val TABLE_COLORS = "Colors"
    }

    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun checkIfProjectExists(name: String?): Boolean {
        val query = "SELECT COUNT(*) FROM $TABLE_INVENTORIES WHERE $COLUMN_NAME LIKE \"$name\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val result = cursor.getInt(0)
        cursor.close()
        db.close()
        return result != 0
    }

    fun getInventoriesNames(showArchived: Boolean): List<String> {
        val condition = if (showArchived) ""
        else "WHERE $COLUMN_ACTIVE = 1"
        val query = "SELECT $COLUMN_NAME FROM $TABLE_INVENTORIES $condition ORDER BY $COLUMN_ACTIVE DESC"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var list = mutableListOf<String>()

        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        return list
    }

    fun addProject(name: String?) {
        val values = ContentValues()
        values.put(COLUMN_LAST_ACCESSED, 0)
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_LAST_ACCESSED, System.currentTimeMillis() / 1000)
        val db = this.writableDatabase
        db.insert(TABLE_INVENTORIES, null, values)
        db.close()
    }

    fun addInventoryPart(attributes: NodeList, projectName: String?) {
        val values = ContentValues()
        val inventoryId = getInventoryId(projectName)
        val typeId = getTypeId(attributes.item(1).textContent.toString().trim())
        val db = this.writableDatabase
        values.put(COLUMN_INVENTORY_ID, inventoryId)
        values.put(COLUMN_TYPE_ID, typeId)
        values.put(COLUMN_ITEM_ID, attributes.item(3).textContent.toString().trim())
        values.put(COLUMN_QUANTITY_IN_SET, Integer.parseInt(attributes.item(5).textContent.toString()))
        values.put(COLUMN_COLOR_ID, Integer.parseInt(attributes.item(7).textContent.toString()))
        db.insert(TABLE_INVENTORIES_PARTS, null, values)
        db.close()
    }

    private fun getInventoryId(projectName: String?) : Int {
        val query = "SELECT $COLUMN_ID FROM $TABLE_INVENTORIES WHERE name LIKE \"$projectName\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val toReturn = Integer.parseInt(cursor.getString(0))
        cursor.close()
        db.close()
        return toReturn
    }

    fun getTypeId(typeCode: String) : Int {
        val query = "SELECT $COLUMN_ID FROM $TABLE_ITEM_TYPES WHERE code LIKE \"$typeCode\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val toReturn = Integer.parseInt(cursor.getString(0))
        cursor.close()
        db.close()
        return toReturn
    }

    fun getNumberOfInventoryParts(projectName: String): Int {
        val inventoryId = getInventoryId(projectName)
        val query = "SELECT COUNT(*) FROM $TABLE_INVENTORIES_PARTS WHERE InventoryId = $inventoryId"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val toReturn = cursor.getInt(0)
        cursor.close()
        db.close()
        return toReturn
    }

    fun updateLastModified(projectName: String) {
        val time = System.currentTimeMillis() / 1000
        val query = "UPDATE $TABLE_INVENTORIES SET $COLUMN_LAST_ACCESSED = $time WHERE $COLUMN_NAME = \"$projectName\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        cursor.close()
        db.close()
    }

    fun getItemInfo(projectName: String): MutableList<MutableList<String>> {
        val inventoryId = getInventoryId(projectName)
        val query = "SELECT ip.QuantityInSet, ip.QuantityInStore, ip.ItemID, col.name, p.name, ip.TypeID " +
                "FROM InventoriesParts AS ip INNER JOIN Parts AS p ON ip.itemID = p.code INNER JOIN COLORS AS col ON col.code = ip.colorid " +
                "WHERE ip.InventoryID = $inventoryId " +
                "ORDER BY $COLUMN_ITEM_ID, ip.$COLUMN_QUANTITY_IN_SET - ip.$COLUMN_QUANTITY_IN_STORE DESC"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val info = MutableList<MutableList<String>>(cursor.count) {
            mutableListOf()
        }
        while (cursor.moveToNext()) {
            for (i in 0..5) {
                info[i].add(cursor.getString(i))
            }
        }
        cursor.close()
        db.close()
        return info
    }

    fun getItemCode(itemId: String, itemColor: String): String? {
        val query = "SELECT cod.code " +
                "FROM $TABLE_PARTS AS p INNER JOIN $TABLE_CODES cod ON p.id = cod.itemID INNER JOIN $TABLE_COLORS AS col ON cod.colorId = col.id "+
                "WHERE col.$COLUMN_NAME = \"$itemColor\" AND p.code = \"$itemId\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val toReturn = if (cursor.count == 0) null
        else cursor.getString(0)
        cursor.close()
        db.close()
        return toReturn
    }

    fun getColorId(color: String): String {
        val query = "SELECT code FROM $TABLE_COLORS WHERE name=\"$color\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val toReturn = cursor.getString(0)
        cursor.close()
        db.close()
        return toReturn
    }

    fun updatePart(projectName: String, collected: Int, itemId: String, colorName: String) {
        val projectId = getInventoryId(projectName)
        val colorId = getColorId(colorName)
        val query = "UPDATE $TABLE_INVENTORIES_PARTS SET $COLUMN_QUANTITY_IN_STORE=$collected " +
                "WHERE $COLUMN_INVENTORY_ID=$projectId AND $COLUMN_ITEM_ID=\"$itemId\" AND $COLUMN_COLOR_ID=$colorId"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        cursor.close()
        db.close()
    }

    fun markAsArchived(projectName: String) {
        val projectId = getInventoryId(projectName)
        val query = "UPDATE $TABLE_INVENTORIES SET $COLUMN_ACTIVE=0 WHERE $COLUMN_ID = $projectId"
        val cursor = this.writableDatabase.rawQuery(query, null)
        cursor.moveToFirst()
        cursor.close()
        this.writableDatabase.close()
    }

    fun getItemType(id: String): String {
        val query = "SELECT code FROM ItemTypes WHERE id=$id"
        val cursor = this.writableDatabase.rawQuery(query, null)
        cursor.moveToFirst()
        val toReturn = cursor.getString(0)
        cursor.close()
        this.writableDatabase.close()
        return toReturn
    }
}