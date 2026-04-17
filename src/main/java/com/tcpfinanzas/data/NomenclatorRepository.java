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
            String query = "SELECT section, extructure, code, description FROM cnae " +
                "WHERE code LIKE '%" + term + "%' " +
                "OR description LIKE '%" + term.toUpperCase() + "%' " +
                "OR section LIKE '%" + term.toUpperCase() + "%' " +
                "OR extructure LIKE '%" + term.toUpperCase() + "%' " +
                "ORDER BY code LIMIT 100";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                CnaeItem item = new CnaeItem();
                item.section = rs.getString("section");
                item.structure = rs.getString("extructure");
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
                "SELECT ac.code as category_code, ac.name as category_name, " +
                "asc_.code as subcategory_code, asc_.name as subcategory_name, " +
                "aa.display_code, aa.name as account_name, aa.nature as account_nature " +
                "FROM account_accounts aa " +
                "JOIN account_categories ac ON aa.category_id = ac.id " +
                "LEFT JOIN account_subcategories asc_ ON aa.subcategory_id = asc_.id " +
                "WHERE 1=1 "
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
                item.itemType = "Cuenta";
                item.categoryCode = rs.getString("category_code");
                item.categoryName = rs.getString("category_name");
                item.subcategoryCode = rs.getString("subcategory_code") != null ? rs.getString("subcategory_code") : "";
                item.subcategoryName = rs.getString("subcategory_name") != null ? rs.getString("subcategory_name") : "";
                item.accountCode = rs.getString("display_code");
                item.accountName = rs.getString("account_name");
                item.accountNature = rs.getString("account_nature");
                item.subaccountCode = "";
                item.subaccountName = "";
                item.subaccountNature = "";
                item.displayCode = rs.getString("display_code");
                item.displayName = rs.getString("account_name");
                item.displayNature = rs.getString("account_nature");
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
            String query = "SELECT asc_.code, asc_.name FROM account_subcategories asc_ " +
                "JOIN account_categories ac ON asc_.category_id = ac.id";
            if (categoryCode != null && !categoryCode.isEmpty()) {
                query += " WHERE ac.code = '" + categoryCode + "'";
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