package com.tcpfinanzas.data;

import com.tcpfinanzas.model.AccountingCategory;
import com.tcpfinanzas.model.AccountingItem;
import com.tcpfinanzas.model.AccountingSubcategory;
import com.tcpfinanzas.model.CnaeItem;
import com.tcpfinanzas.model.NomenclatorType;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NomenclatorRepository {
    private static NomenclatorRepository instance;
    private Connection connection;
    private final Map<String, Runnable> listeners = new HashMap<>();

    private NomenclatorRepository() {
        initDatabase();
    }

    public static NomenclatorRepository getInstance() {
        if (instance == null) {
            instance = new NomenclatorRepository();
        }
        return instance;
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");

            InputStream dbStream = getClass().getResourceAsStream("/database.db");
            if (dbStream != null) {
                java.io.File tempFile = java.io.File.createTempFile("nomenclator", ".db");
                tempFile.deleteOnExit();
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dbStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath());
            } else {
                System.err.println("No se encontró la base de datos en resources");
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar base de datos: " + e.getMessage());
        }
    }

    public void addListener(String key, Runnable listener) {
        listeners.put(key, listener);
    }

    public void removeListener(String key) {
        listeners.remove(key);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners.values()) {
            if (listener != null) {
                listener.run();
            }
        }
    }

    public List<CnaeItem> searchCnae(String term) {
        List<CnaeItem> results = new ArrayList<>();
        if (connection == null || term == null) return results;

        try (Statement stmt = connection.createStatement()) {
            String query = "SELECT section, structure, code, description FROM cnae " +
                "WHERE code LIKE '%" + term + "%' " +
                "OR description LIKE '%" + term.toUpperCase() + "%' " +
                "OR section LIKE '%" + term.toUpperCase() + "%' " +
                "OR structure LIKE '%" + term.toUpperCase() + "%' " +
                "ORDER BY code LIMIT 100";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                CnaeItem item = new CnaeItem();
                item.section = rs.getString("section");
                item.structure = rs.getString("structure");
                item.code = rs.getString("code");
                item.description = rs.getString("description");
                results.add(item);
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("Error en búsqueda CNAE: " + e.getMessage());
        }
        return results;
    }

    public List<AccountingItem> searchAccounting(String term, String categoryCode, String subcategoryCode) {
        List<AccountingItem> results = new ArrayList<>();
        if (connection == null || term == null) return results;

        try (Statement stmt = connection.createStatement()) {
            StringBuilder query = new StringBuilder(
                "SELECT item_type, category_code, category_name, subcategory_code, subcategory_name, " +
                "account_code, account_name, account_nature, subaccount_code, subaccount_name, subaccount_nature, " +
                "display_code, display_name, display_nature FROM accounting_items WHERE 1=1 "
            );

            if (term != null && !term.isEmpty()) {
                query.append("AND (display_name LIKE '%").append(term.toUpperCase()).append("%' ");
                query.append("OR display_code LIKE '%").append(term).append("%' ");
                query.append("OR account_name LIKE '%").append(term.toUpperCase()).append("%') ");
            }

            if (categoryCode != null && !categoryCode.isEmpty()) {
                query.append("AND category_code = '").append(categoryCode).append("' ");
            }

            if (subcategoryCode != null && !subcategoryCode.isEmpty()) {
                query.append("AND subcategory_code = '").append(subcategoryCode).append("' ");
            }

            query.append("ORDER BY display_code LIMIT 100");

            ResultSet rs = stmt.executeQuery(query.toString());
            while (rs.next()) {
                AccountingItem item = new AccountingItem();
                item.itemType = rs.getString("item_type");
                item.categoryCode = rs.getString("category_code");
                item.categoryName = rs.getString("category_name");
                item.subcategoryCode = rs.getString("subcategory_code");
                item.subcategoryName = rs.getString("subcategory_name");
                item.accountCode = rs.getString("account_code");
                item.accountName = rs.getString("account_name");
                item.accountNature = rs.getString("account_nature");
                item.subaccountCode = rs.getString("subaccount_code");
                item.subaccountName = rs.getString("subaccount_name");
                item.subaccountNature = rs.getString("subaccount_nature");
                item.displayCode = rs.getString("display_code");
                item.displayName = rs.getString("display_name");
                item.displayNature = rs.getString("display_nature");
                results.add(item);
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("Error en búsqueda contable: " + e.getMessage());
        }
        return results;
    }

    public List<AccountingCategory> getAccountingCategories() {
        List<AccountingCategory> results = new ArrayList<>();
        if (connection == null) return results;

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT code, name FROM account_categories ORDER BY sort_order"
            );
            while (rs.next()) {
                AccountingCategory cat = new AccountingCategory();
                cat.code = rs.getString("code");
                cat.name = rs.getString("name");
                results.add(cat);
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("Error en categorías: " + e.getMessage());
        }
        return results;
    }

    public List<AccountingSubcategory> getAccountingSubcategories(String categoryCode) {
        List<AccountingSubcategory> results = new ArrayList<>();
        if (connection == null) return results;

        try (Statement stmt = connection.createStatement()) {
            String query = "SELECT code, name FROM account_subcategories";
            if (categoryCode != null && !categoryCode.isEmpty()) {
                query += " WHERE category_code = '" + categoryCode + "'";
            }
            query += " ORDER BY sort_order";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                AccountingSubcategory sub = new AccountingSubcategory();
                sub.code = rs.getString("code");
                sub.name = rs.getString("name");
                results.add(sub);
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("Error en subcategorías: " + e.getMessage());
        }
        return results;
    }
}