package org.sqlproc.dsl.ui.templates;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.xtext.common.types.JvmType;
import org.sqlproc.dsl.processorDsl.Artifacts;
import org.sqlproc.dsl.property.ModelProperty;
import org.sqlproc.dsl.property.PojoAttrType;
import org.sqlproc.dsl.property.PojoAttribute;
import org.sqlproc.dsl.resolver.DbColumn;
import org.sqlproc.dsl.resolver.DbExport;
import org.sqlproc.dsl.resolver.DbImport;
import org.sqlproc.dsl.resolver.DbIndex;
import org.sqlproc.dsl.resolver.DbIndex.DbIndexDetail;

public class TablePojoConverter {

    private boolean debug = false;

    protected enum PrimitiveType {
        BOOLEAN, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, CHAR, BYTE_ARRAY, CHAR_ARRAY;

        public String getName() {
            if (this == BYTE_ARRAY)
                return "byte []";
            if (this == CHAR_ARRAY)
                return "char []";
            return name().toLowerCase();
        }
    }

    protected static final String METHOD_TO_STRING = "toString";
    protected static final String METHOD_HASH_CODE = "hashCode";
    protected static final String METHOD_EQUALS = "equals";
    protected static final String METHOD_IS_DEF = "isDef";
    protected static final String ENUM_IS_DEF = "enumDef";
    protected static final String METHOD_TO_INIT = "toInit";
    protected static final String ENUM_TO_INIT = "enumInit";
    protected static final String COLLECTION_LIST = "java.util.List";

    protected String suffix;
    protected Set<String> finalEntities;
    protected Map<String, PojoAttrType> sqlTypes = new HashMap<String, PojoAttrType>();
    protected Map<String, Map<String, PojoAttrType>> tableTypes = new HashMap<String, Map<String, PojoAttrType>>();
    protected Map<String, Map<String, PojoAttrType>> columnTypes = new HashMap<String, Map<String, PojoAttrType>>();
    protected Map<String, String> tableNames = new HashMap<String, String>();
    protected Map<String, Map<String, String>> columnNames = new HashMap<String, Map<String, String>>();
    protected Set<String> ignoreTables = new HashSet<String>();
    protected Set<String> onlyTables = new HashSet<String>();
    protected Set<String> notAbstractTables = new HashSet<String>();
    protected Map<String, Set<String>> ignoreColumns = new HashMap<String, Set<String>>();
    protected Map<String, Set<String>> requiredColumns = new HashMap<String, Set<String>>();
    protected Map<String, Set<String>> notRequiredColumns = new HashMap<String, Set<String>>();
    protected Map<String, Map<String, PojoAttrType>> createColumns = new HashMap<String, Map<String, PojoAttrType>>();
    protected Map<String, Map<String, Map<String, String>>> ignoreExports = new HashMap<String, Map<String, Map<String, String>>>();
    protected Map<String, Map<String, Map<String, String>>> ignoreImports = new HashMap<String, Map<String, Map<String, String>>>();
    protected Map<String, Map<String, Map<String, String>>> createExports = new HashMap<String, Map<String, Map<String, String>>>();
    protected Map<String, Map<String, Map<String, String>>> createImports = new HashMap<String, Map<String, Map<String, String>>>();
    protected Map<String, Map<String, Map<String, String>>> inheritImports = new HashMap<String, Map<String, Map<String, String>>>();
    protected Map<String, Map<String, Map<String, String>>> manyToManyImports = new HashMap<String, Map<String, Map<String, String>>>();
    protected Map<String, Map<String, Map<String, List<String>>>> inheritance = new HashMap<String, Map<String, Map<String, List<String>>>>();
    protected Map<String, String> inheritanceColumns = new HashMap<String, String>();
    protected Set<String> generateMethods = new HashSet<String>();
    protected Map<String, JvmType> toImplements = new HashMap<String, JvmType>();
    protected JvmType toExtends = null;
    protected Map<String, List<String>> joinTables = new HashMap<String, List<String>>();
    protected boolean doGenerateWrappers;
    protected String implementationPackage;
    protected boolean makeItFinal;
    protected String versionColumn;
    protected Map<String, String> versionColumns = new HashMap<String, String>();

    protected Map<String, Map<String, PojoAttribute>> pojos = new TreeMap<String, Map<String, PojoAttribute>>();
    protected Map<String, String> pojoExtends = new HashMap<String, String>();
    protected Map<String, Set<String>> pojoInheritanceDiscriminator = new HashMap<String, Set<String>>();
    protected Map<String, Set<String>> pojoInheritanceSimple = new HashMap<String, Set<String>>();
    protected Map<String, String> pojoDiscriminators = new HashMap<String, String>();

    public TablePojoConverter() {
    }

    public TablePojoConverter(ModelProperty modelProperty, Artifacts artifacts, String suffix, Set<String> finalEntities) {

        this.suffix = (suffix != null) ? suffix : "";
        this.finalEntities = finalEntities;

        Map<String, PojoAttrType> sqlTypes = modelProperty.getSqlTypes(artifacts);
        if (sqlTypes != null) {
            this.sqlTypes.putAll(sqlTypes);
        }
        Map<String, Map<String, PojoAttrType>> tableTypes = modelProperty.getTableTypes(artifacts);
        if (tableTypes != null) {
            this.tableTypes.putAll(tableTypes);
        }
        Map<String, Map<String, PojoAttrType>> columnTypes = modelProperty.getColumnTypes(artifacts);
        if (columnTypes != null) {
            this.columnTypes.putAll(columnTypes);
        }
        Map<String, String> tableNames = modelProperty.getTableNames(artifacts);
        if (tableNames != null) {
            this.tableNames.putAll(tableNames);
        }
        Map<String, Map<String, String>> columnNames = modelProperty.getColumnNames(artifacts);
        if (columnNames != null) {
            this.columnNames.putAll(columnNames);
        }
        Set<String> ignoreTables = modelProperty.getIgnoreTables(artifacts);
        if (ignoreTables != null) {
            this.ignoreTables.addAll(ignoreTables);
        }
        Set<String> onlyTables = modelProperty.getOnlyTables(artifacts);
        if (onlyTables != null) {
            this.onlyTables.addAll(onlyTables);
        }
        Set<String> notAbstractTables = modelProperty.getNotAbstractTables(artifacts);
        if (onlyTables != null) {
            this.notAbstractTables.addAll(notAbstractTables);
        }
        Map<String, Set<String>> ignoreColumns = modelProperty.getIgnoreColumns(artifacts);
        if (ignoreColumns != null) {
            this.ignoreColumns.putAll(ignoreColumns);
        }
        Map<String, Set<String>> requiredColumns = modelProperty.getRequiredColumns(artifacts);
        if (requiredColumns != null) {
            this.requiredColumns.putAll(requiredColumns);
        }
        Map<String, Set<String>> notRequiredColumns = modelProperty.getNotRequiredColumns(artifacts);
        if (notRequiredColumns != null) {
            this.notRequiredColumns.putAll(notRequiredColumns);
        }
        Map<String, Map<String, PojoAttrType>> createColumns = modelProperty.getCreateColumns(artifacts);
        if (createColumns != null) {
            this.createColumns.putAll(createColumns);
        }
        Map<String, Map<String, Map<String, String>>> ignoreExports = modelProperty.getIgnoreExports(artifacts);
        if (ignoreExports != null) {
            this.ignoreExports.putAll(ignoreExports);
        }
        Map<String, Map<String, Map<String, String>>> ignoreImports = modelProperty.getIgnoreImports(artifacts);
        if (ignoreImports != null) {
            this.ignoreImports.putAll(ignoreImports);
        }
        Map<String, Map<String, Map<String, String>>> createExports = modelProperty.getCreateExports(artifacts);
        if (createExports != null) {
            this.createExports.putAll(createExports);
        }
        Map<String, Map<String, Map<String, String>>> createImports = modelProperty.getCreateImports(artifacts);
        if (createImports != null) {
            this.createImports.putAll(createImports);
        }
        Map<String, Map<String, Map<String, String>>> inheritImports = modelProperty.getInheritImports(artifacts);
        if (inheritImports != null) {
            this.inheritImports.putAll(inheritImports);
        }
        Map<String, Map<String, Map<String, String>>> manyToManyImports = modelProperty.getManyToManyImports(artifacts);
        if (manyToManyImports != null) {
            this.manyToManyImports.putAll(manyToManyImports);
        }
        Map<String, Map<String, Map<String, List<String>>>> inheritance = modelProperty.getInheritance(artifacts);
        if (inheritance != null) {
            this.inheritance.putAll(inheritance);
        }
        Map<String, String> inheritanceColumns = modelProperty.getInheritanceColumns(artifacts);
        if (inheritanceColumns != null) {
            this.inheritanceColumns.putAll(inheritanceColumns);
        }
        Set<String> generateMethods = modelProperty.getGenerateMethods(artifacts);
        if (generateMethods != null) {
            this.generateMethods.addAll(generateMethods);
        }
        Map<String, JvmType> toImplements = modelProperty.getToImplements(artifacts);
        if (toImplements != null) {
            this.toImplements.putAll(toImplements);
        }
        this.toExtends = modelProperty.getToExtends(artifacts);
        Map<String, List<String>> joinTables = modelProperty.getJoinTables(artifacts);
        if (joinTables != null) {
            this.joinTables.putAll(joinTables);
        }
        this.doGenerateWrappers = modelProperty.isDoGenerateWrappers(artifacts);
        this.implementationPackage = modelProperty.getImplementationPackage(artifacts);
        this.makeItFinal = modelProperty.isMakeItFinal(artifacts);
        this.versionColumn = modelProperty.getVersionColumn(artifacts);
        Map<String, String> versionColumns = modelProperty.getVersionColumns(artifacts);
        if (versionColumns != null) {
            this.versionColumns.putAll(versionColumns);
        }

        for (Map.Entry<String, Map<String, Map<String, String>>> inheritImport : this.inheritImports.entrySet()) {
            for (Map.Entry<String, Map<String, String>> inherit : inheritImport.getValue().entrySet()) {
                for (Map.Entry<String, String> tabcol : inherit.getValue().entrySet()) {
                    if (!this.ignoreExports.containsKey(tabcol.getKey()))
                        this.ignoreExports.put(tabcol.getKey(), new HashMap<String, Map<String, String>>());
                    if (!this.ignoreExports.get(tabcol.getKey()).containsKey(tabcol.getValue()))
                        this.ignoreExports.get(tabcol.getKey()).put(tabcol.getValue(), new HashMap<String, String>());
                    this.ignoreExports.get(tabcol.getKey()).get(tabcol.getValue()).put(inheritImport.getKey(), null);
                    if (!this.ignoreColumns.containsKey(inheritImport.getKey()))
                        this.ignoreColumns.put(inheritImport.getKey(), new HashSet<String>());
                    this.ignoreColumns.get(inheritImport.getKey()).add(inherit.getKey());
                }
            }
        }

        if (debug) {
            System.out.println("finalEntities " + this.finalEntities);
            System.out.println("sqlTypes " + this.sqlTypes);
            System.out.println("tableTypes " + this.tableTypes);
            System.out.println("columnTypes " + this.columnTypes);
            System.out.println("tableNames " + this.tableNames);
            System.out.println("columnNames " + this.columnNames);
            System.out.println("ignoreTables " + this.ignoreTables);
            System.out.println("onlyTables " + this.onlyTables);
            System.out.println("ignoreColumns " + this.ignoreColumns);
            System.out.println("createColumns " + this.createColumns);
            System.out.println("ignoreExports " + this.ignoreExports);
            System.out.println("ignoreImports " + this.ignoreImports);
            System.out.println("createExports " + this.createExports);
            System.out.println("createImports " + this.createImports);
            System.out.println("inheritImports " + this.inheritImports);
            System.out.println("manyToManyImports " + this.manyToManyImports);
            System.out.println("inheritance " + this.inheritance);
            System.out.println("inheritanceColumns " + this.inheritanceColumns);
            System.out.println("generateMethods " + this.generateMethods);
            System.out.println("toImplements " + this.toImplements);
            System.out.println("toExtends " + this.toExtends);
            System.out.println("joinTables " + this.joinTables);
            System.out.println("doGenerateWrappers " + this.doGenerateWrappers);
            System.out.println("implementationPackage " + this.implementationPackage);
            System.out.println("makeItFinal " + this.makeItFinal);
            System.out.println("versionColumn " + this.versionColumn);
            System.out.println("versionColumns " + this.versionColumns);
        }
    }

    public void addTableDefinition(String table, List<DbColumn> dbColumns, List<String> dbPrimaryKeys,
            List<DbExport> dbExports, List<DbImport> dbImports, List<DbIndex> dbIndexes) {
        if (table == null || dbColumns == null)
            return;
        Map<String, PojoAttribute> attributes = new LinkedHashMap<String, PojoAttribute>();
        for (DbColumn dbColumn : dbColumns) {
            PojoAttribute attribute = convertDbColumnDefinition(table, dbColumn);
            if (attribute != null) {
                attributes.put(dbColumn.getName(), attribute);
            } else {
                attribute = convertDbColumnDefault(table, dbColumn);
                if (attribute != null)
                    attributes.put(dbColumn.getName(), attribute);
            }
            if (dbPrimaryKeys.contains(dbColumn.getName())) {
                attribute.setPrimaryKey(true);
            }

        }
        if (createColumns.containsKey(table)) {
            for (Map.Entry<String, PojoAttrType> createColumn : createColumns.get(table).entrySet()) {
                PojoAttribute attribute = convertDbColumnDefinition(createColumn.getKey(), createColumn.getValue());
                attributes.put(createColumn.getKey(), attribute);
            }
        }
        pojos.put(table, attributes);
        for (DbImport dbImport : dbImports) {
            if (ignoreImports.containsKey(table)
                    && (ignoreImports.get(table) == null || ignoreImports.get(table)
                            .containsKey(dbImport.getFkColumn())
                            && ignoreImports.get(table).get(dbImport.getFkColumn()).containsKey(dbImport.getPkTable())))
                continue;
            if (manyToManyImports.containsKey(table)) {
                PojoAttribute attribute = attributes.get(dbImport.getFkColumn());
                attribute.setM2mTable(dbImport.getPkTable());
                attribute.setPkColumn(dbImport.getPkColumn());
            } else if (inheritImports.containsKey(table)
                    && inheritImports.get(table).containsKey(dbImport.getFkColumn())
                    && inheritImports.get(table).get(dbImport.getFkColumn()).containsKey(dbImport.getPkTable())) {
                PojoAttribute attribute = attributes.get(dbImport.getFkColumn());
                attribute.setParentTable(dbImport.getPkTable());
                attribute.setPkColumn(dbImport.getPkColumn());
            } else {
                PojoAttribute attribute = attributes.get(dbImport.getFkColumn());
                attribute.setPkTable(dbImport.getPkTable());
                attribute.setPkColumn(dbImport.getPkColumn());
            }
        }
        if (createImports.containsKey(table)) {
            for (Map.Entry<String, Map<String, String>> pentry : createImports.get(table).entrySet()) {
                PojoAttribute attribute = attributes.get(pentry.getKey());
                for (Entry<String, String> pkEntry : pentry.getValue().entrySet()) {
                    attribute.setPkTable(pkEntry.getKey());
                    attribute.setPkColumn(pkEntry.getValue());
                }
            }
        }
        for (DbExport dbExport : dbExports) {
            // AAA1 PERSON_LIBRARY MEDIA_ID MEDIA_ID MEDIA
            // AAA1 PERSON_LIBRARY PERSON_ID PERSON_ID PERSON
            // AAA3 PERSON PERSON ID PERSON_LIBRARY PERSON_ID
            // AAA3 MEDIA MEDIA ID PERSON_LIBRARY MEDIA_ID
            // pojogen table many-to-many PERSON_LIBRARY ID->MEDIA->LIBRARY;
            // System.out.println("AAA3 " + table + " " + dbExport.getPkTable() + " " + dbExport.getPkColumn() + " " +
            // dbExport.getFkTable() + " " + dbExport.getFkColumn());
            if (ignoreExports.containsKey(table) && ignoreExports.get(table).containsKey(dbExport.getPkColumn())
                    && ignoreExports.get(table).get(dbExport.getPkColumn()).containsKey(dbExport.getFkTable()))
                continue;
            if (manyToManyImports.containsKey(dbExport.getFkTable())) {
                if (manyToManyImports.get(dbExport.getFkTable()).containsKey(dbExport.getPkColumn())) {
                    for (Map.Entry<String, String> manyToMany : manyToManyImports.get(dbExport.getFkTable())
                            .get(dbExport.getPkColumn()).entrySet()) {
                        String fkTable = manyToMany.getKey();
                        String fkColumn = manyToMany.getValue();
                        if (!dbExport.getPkTable().equals(fkTable)) {
                            PojoAttribute attribute = attributes.get(dbExport.getPkColumn());
                            attribute.getFkTables().put(fkTable, fkColumn);
                            attribute.getM2Tables().put(fkTable, dbExport.getFkTable());
                        }
                    }
                }
            } else {
                PojoAttribute attribute = attributes.get(dbExport.getPkColumn());
                attribute.getFkTables().put(dbExport.getFkTable(), null);
                attribute.getFkColumns().put(dbExport.getFkTable(), dbExport.getFkColumn());
            }
        }
        if (createExports.containsKey(table)) {
            for (Map.Entry<String, Map<String, String>> pentry : createExports.get(table).entrySet()) {
                PojoAttribute attribute = attributes.get(pentry.getKey());
                for (Map.Entry<String, String> fk : pentry.getValue().entrySet()) {
                    attribute.getFkTables().put(fk.getKey(), null);
                    attribute.getFkColumns().put(fk.getKey(), fk.getValue());
                }
            }
        }
        if (inheritance.containsKey(table)) {
            // pojogen inherit discriminator BILLING_DETAILS BA->BANK_ACCOUNT->BA_ACCOUNT CC->CREDIT_CARD->CC_NUMBER;
            // pojogen rename columns BANK_ACCOUNT BA_ACCOUNT->ACCOUNT;
            // pojogen rename columns CREDIT_CARD CC_NUMBER->NUMBER;
            Map<String, PojoAttribute> allInheritedAttributes = new LinkedHashMap<String, PojoAttribute>();
            for (Map.Entry<String, Map<String, List<String>>> inheritance1 : inheritance.get(table).entrySet()) {
                String discriminator = inheritance1.getKey();
                Map<String, PojoAttribute> inheritedAttributes = new LinkedHashMap<String, PojoAttribute>();
                for (Map.Entry<String, List<String>> tabcols : inheritance1.getValue().entrySet()) {
                    String inheritedTable = tabcols.getKey();
                    for (String dbColumn : tabcols.getValue()) {
                        if (attributes.containsKey(dbColumn)) {
                            inheritedAttributes.put(dbColumn, attributes.get(dbColumn));
                        }
                    }
                    allInheritedAttributes.putAll(inheritedAttributes);
                    pojos.put(inheritedTable, inheritedAttributes);
                    pojoDiscriminators.put(inheritedTable, discriminator);
                    pojoExtends.put(inheritedTable, table);
                    break;
                }
            }
            for (String dbColumn : allInheritedAttributes.keySet()) {
                attributes.remove(dbColumn);
            }
            pojoInheritanceDiscriminator.put(table, new LinkedHashSet<String>());
        }

        int currIndex = 1;
        for (DbIndex dbIndex : dbIndexes) {
            if (dbIndex.getColumns().size() != 1)
                continue;
            DbIndexDetail dbIndexDetail = dbIndex.getColumns().get(0);
            PojoAttribute attr = attributes.get(dbIndexDetail.getColname());
            if (attr == null)
                continue;
            attr.setIndex(currIndex++);
        }
    }

    public void joinTables() {
        for (String table : joinTables.keySet()) {
            if (!pojos.containsKey(table))
                continue;
            Stack<String> stack = new Stack<String>();
            stack.push(table);
            for (String table1 : joinTables.get(table)) {
                if (!pojos.containsKey(table1))
                    break;
                stack.push(table1);
            }
            if (stack.size() != joinTables.get(table).size() + 1)
                continue;

            String table0 = stack.pop();
            Set<String> allAttributes = new HashSet<String>();
            allAttributes.addAll(pojos.get(table0).keySet());
            if (tableNames.containsKey(table0))
                table0 = tableNames.get(table0);
            while (stack.size() > 0) {
                String table1 = stack.pop();
                String newTable = (tableNames.containsKey(table1) ? tableNames.get(table1) : table1) + "_" + table0;
                Map<String, PojoAttribute> newAttributes = new LinkedHashMap<String, PojoAttribute>();
                for (Entry<String, PojoAttribute> attr : pojos.get(table1).entrySet()) {
                    if (!allAttributes.contains(attr.getKey())) {
                        newAttributes.put(attr.getKey(), attr.getValue());
                        allAttributes.add(attr.getKey());
                    }
                }
                pojos.put(newTable, newAttributes);
                pojoExtends.put(newTable, table0);
                if (!onlyTables.isEmpty())
                    onlyTables.add(newTable);
                table0 = newTable;
            }
        }
    }

    public void resolveReferencesOnKeys() {
        for (String pojo : pojos.keySet()) {
            Map<String, PojoAttribute> newAttributes = new HashMap<String, PojoAttribute>();
            for (Entry<String, PojoAttribute> entry : pojos.get(pojo).entrySet()) {
                PojoAttribute attribute = entry.getValue();
                if (attribute.getPkTable() != null) {
                    if (pojos.containsKey(attribute.getPkTable())) {
                        attribute.setDependencyClassName(tableToCamelCase(attribute.getPkTable()));
                        attribute.setRef(attribute.getPkTable());
                        if (attribute.getName().length() >= 3) {
                            if (attribute.getName().startsWith("id")) {
                                attribute.setName(lowerFirstChar(attribute.getName().substring(2)));
                            } else if (attribute.getName().endsWith("Id")) {
                                attribute.setName(attribute.getName().substring(0, attribute.getName().length() - 2));
                            }
                        }
                    }
                }
                if (attribute.getParentTable() != null) {
                    if (pojos.containsKey(attribute.getParentTable())) {
                        pojoExtends.put(pojo, attribute.getParentTable());
                        pojoInheritanceSimple.put(attribute.getParentTable(), new LinkedHashSet<String>());
                    }
                }
                for (Map.Entry<String, String> fk : attribute.getFkTables().entrySet()) {
                    if (pojos.containsKey(fk.getKey())) {
                        String referName = null;
                        if (fk.getValue() != null) {
                            referName = columnToCamelCase(fk.getValue());
                        } else {
                            referName = lowerFirstChar(tableToCamelCase(fk.getKey()));
                            if (!referName.endsWith("s")) {
                                if (referName.endsWith("y")) {
                                    referName = referName.substring(0, referName.length() - 1);
                                    referName += "ies";
                                } else {
                                    referName += "s";
                                }
                            }
                        }
                        PojoAttribute attrib = new PojoAttribute();
                        attrib.setName(referName);
                        if (attribute.getM2Tables().containsKey(fk.getKey())) {
                            attrib.setManyToManyColumn(entry.getKey());
                            attrib.setManyToManyTable(fk.getKey());
                        } else if (attribute.getFkColumns().containsKey(fk.getKey())) {
                            attrib.setOneToManyColumn(entry.getKey());
                            attrib.setOneToManyTable(fk.getKey());
                        }
                        attrib.setClassName(COLLECTION_LIST + " <:" + tableToCamelCase(fk.getKey()) + ">");
                        attrib.setRef(fk.getKey());
                        String dbColumnName = columnToDbConv(attrib.getName());
                        newAttributes.put(dbColumnName, attrib);
                    }
                }
            }
            if (!newAttributes.isEmpty())
                pojos.get(pojo).putAll(newAttributes);
        }
        for (Entry<String, String> entry : pojoExtends.entrySet()) {
            if (pojoInheritanceDiscriminator.containsKey(entry.getValue())) {
                pojoInheritanceDiscriminator.get(entry.getValue()).add(entry.getKey());
            } else if (pojoInheritanceSimple.containsKey(entry.getValue())) {
                pojoInheritanceSimple.get(entry.getValue()).add(entry.getKey());
            }
        }
    }

    protected String lowerFirstChar(String s) {
        if (s == null)
            return null;
        String ss = s.substring(0, 1).toLowerCase();
        if (s.length() == 1)
            return ss;
        ss += s.substring(1);
        return ss;
    }

    public void resolveReferencesOnConvention() {
        for (String pojo : pojos.keySet()) {
            for (PojoAttribute attribute : pojos.get(pojo).values()) {
                if (attribute.getName().startsWith("id") && attribute.getName().length() > 2) {
                    String className = attribute.getName().substring(2);
                    if (pojo.equals(className))
                        continue;
                    Map<String, PojoAttribute> referPojoAttr = pojos.get(className);
                    if (referPojoAttr != null) {
                        String name = className.substring(0, 1).toLowerCase();
                        if (className.length() > 1)
                            name += className.substring(1);
                        attribute.setName(name);
                        attribute.setPrimitive(false);
                        attribute.setDependencyClassName(className);
                        // reverse dependency
                        String referName = pojo.substring(0, 1).toLowerCase();
                        if (className.length() > 1)
                            referName += pojo.substring(1);
                        PojoAttribute attrib = new PojoAttribute();
                        attrib.setName(referName + "s");
                        attrib.setClassName(COLLECTION_LIST + " <:" + pojo + ">");
                        referPojoAttr.put(attrib.getName(), attrib);
                    }
                }
            }
        }
    }

    public String getPojoDefinitions() {
        try {
            if (debug) {
                System.out.println("pojos " + this.pojos);
                System.out.println("pojoExtends " + this.pojoExtends);
                System.out.println("pojoInheritanceDiscriminator " + this.pojoInheritanceDiscriminator);
                System.out.println("pojoInheritanceSimple " + this.pojoInheritanceSimple);
                System.out.println("pojoDiscriminators " + this.pojoDiscriminators);
            }

            StringBuilder buffer = new StringBuilder();
            boolean isSerializable = false;
            boolean oneMoreLine = false;
            if (!toImplements.isEmpty()) {
                for (JvmType type : toImplements.values()) {
                    if (type.getIdentifier().endsWith("Serializable")) {
                        isSerializable = true;
                        continue;
                    }
                    buffer.append("\n  implements ").append(type.getIdentifier());
                }
                oneMoreLine = true;
            }
            if (toExtends != null) {
                buffer.append("\n  extends ").append(toExtends.getIdentifier());
                oneMoreLine = true;
            }
            if (implementationPackage != null) {
                buffer.append("\n  implementation-package ").append(implementationPackage);
                oneMoreLine = true;
            }
            if (oneMoreLine) {
                buffer.append("\n");
            }
            for (String pojo : pojos.keySet()) {
                // System.out.println("QQQQQ " + pojo);
                if (!onlyTables.isEmpty() && !onlyTables.contains(pojo))
                    continue;
                if (ignoreTables.contains(pojo))
                    continue;
                String pojoName = tableNames.get(pojo);
                if (pojoName == null)
                    pojoName = pojo;
                if (finalEntities.contains(tableToCamelCase(pojoName)))
                    continue;
                buffer.append("\n  ");
                if (makeItFinal)
                    buffer.append("final ");
                if (pojoInheritanceDiscriminator.containsKey(pojo) || pojoInheritanceSimple.containsKey(pojo)) {
                    if (!notAbstractTables.contains(pojo))
                        buffer.append("abstract ");
                }
                buffer.append("pojo ");
                buffer.append(tableToCamelCase(pojoName));
                if (pojoExtends.containsKey(pojo))
                    buffer.append(" extends ").append(tableToCamelCase(pojoExtends.get(pojo)));
                if (pojoDiscriminators.containsKey(pojo))
                    buffer.append(" discriminator ").append(pojoDiscriminators.get(pojo));
                if (isSerializable)
                    buffer.append(" serializable 1 ");
                buffer.append(" {");
                Set<String> pkeys = new HashSet<String>();
                Set<String> toStr = new HashSet<String>();
                Set<String> isDef = new HashSet<String>();
                Set<String> toInit = new HashSet<String>();
                for (Map.Entry<String, PojoAttribute> pentry : pojos.get(pojo).entrySet()) {
                    // System.out.println("  RRR " + pentry.getKey());
                    if (ignoreColumns.containsKey(pojo) && ignoreColumns.get(pojo).contains(pentry.getKey()))
                        continue;
                    PojoAttribute attribute = pentry.getValue();
                    String name = (columnNames.containsKey(pojo)) ? columnNames.get(pojo).get(pentry.getKey()) : null;
                    if (name == null)
                        name = attribute.getName();
                    else
                        name = columnToCamelCase(name);
                    buffer.append("\n    ").append(name).append(' ');
                    if (attribute.getDependencyClassName() != null) {
                        buffer.append(":: ").append(attribute.getDependencyClassName());
                        toStr.add(name);
                    } else if (attribute.isPrimitive()) {
                        buffer.append('_').append(attribute.getClassName());
                        toStr.add(name);
                    } else {
                        buffer.append(": ").append(attribute.getClassName());
                        if (!attribute.getClassName().startsWith(COLLECTION_LIST))
                            toStr.add(name);
                    }
                    if (attribute.isDef())
                        isDef.add(name);
                    if (attribute.toInit())
                        toInit.add(name);
                    if (inheritanceColumns.containsKey(pojo) && pentry.getKey().equals(inheritanceColumns.get(pojo))) {
                        buffer.append(" discriminator");
                    }
                    boolean optLock = false;
                    if (versionColumns.containsKey(pojo) && pentry.getKey().equals(versionColumns.get(pojo))) {
                        buffer.append(" optLock");
                        optLock = true;
                    } else if (pentry.getKey().equalsIgnoreCase(versionColumn)) {
                        buffer.append(" optLock");
                        optLock = true;
                    }
                    if (!optLock
                            && ((requiredColumns.containsKey(pojo) && requiredColumns.get(pojo).contains(
                                    pentry.getKey())) || (attribute.isRequired() && !attribute.isPrimaryKey()))) {
                        if (!notRequiredColumns.containsKey(pojo)
                                || !notRequiredColumns.get(pojo).contains(pentry.getKey()))
                            buffer.append(" required");
                    }
                    if (attribute.isPrimaryKey()) {
                        buffer.append(" primaryKey");
                        pkeys.add(name);
                    }
                    if (attribute.getIndex() != null) {
                        buffer.append(" index ").append(attribute.getIndex());
                    }
                }
                if (pojoExtends.containsKey(pojo)) {
                    getParentAttrs(pojoExtends.get(pojo), isDef, toInit);
                }
                if (generateMethods.contains(METHOD_EQUALS) && !pkeys.isEmpty()) {
                    buffer.append("\n    ").append(METHOD_EQUALS).append(" :::");
                    for (String name : pkeys) {
                        buffer.append(" ").append(name);
                    }
                }
                if (generateMethods.contains(METHOD_HASH_CODE) && !pkeys.isEmpty()) {
                    buffer.append("\n    ").append(METHOD_HASH_CODE).append(" :::");
                    for (String name : pkeys) {
                        buffer.append(" ").append(name);
                    }
                }
                if (generateMethods.contains(METHOD_TO_INIT)) {
                    buffer.append("\n    ").append(METHOD_TO_INIT).append(" :::");
                    for (String name : toInit) {
                        buffer.append(" ").append(name);
                    }
                } else if (generateMethods.contains(ENUM_TO_INIT) && !toInit.isEmpty()) {
                    buffer.append("\n    ").append(ENUM_TO_INIT).append(" :::");
                    for (String name : toInit) {
                        buffer.append(" ").append(name);
                    }
                }
                if (generateMethods.contains(METHOD_IS_DEF)) {
                    buffer.append("\n    ").append(METHOD_IS_DEF).append(" :::");
                    for (String name : isDef) {
                        buffer.append(" ").append(name);
                    }
                } else if (generateMethods.contains(ENUM_IS_DEF) && !isDef.isEmpty()) {
                    buffer.append("\n    ").append(ENUM_IS_DEF).append(" :::");
                    for (String name : isDef) {
                        buffer.append(" ").append(name);
                    }
                }
                if (generateMethods.contains(METHOD_TO_STRING) && !toStr.isEmpty()) {
                    buffer.append("\n    ").append(METHOD_TO_STRING).append(" :::");
                    for (String name : toStr) {
                        buffer.append(" ").append(name);
                    }
                }
                buffer.append("\n  }\n");
            }
            return buffer.toString();
        } catch (RuntimeException ex) {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            String s = writer.toString();
            return s;
        }
    }

    protected void getParentAttrs(String pojo, Set<String> isDef, Set<String> toInit) {
        for (Map.Entry<String, PojoAttribute> pentry : pojos.get(pojo).entrySet()) {
            if (ignoreColumns.containsKey(pojo) && ignoreColumns.get(pojo).contains(pentry.getKey()))
                continue;
            PojoAttribute attribute = pentry.getValue();
            String name = (columnNames.containsKey(pojo)) ? columnNames.get(pojo).get(pentry.getKey()) : null;
            if (name == null)
                name = attribute.getName();
            else
                name = columnToCamelCase(name);
            if (attribute.isDef())
                isDef.add(name);
            if (attribute.toInit())
                toInit.add(name);
        }
        if (pojoExtends.containsKey(pojo)) {
            getParentAttrs(pojoExtends.get(pojo), isDef, toInit);
        }
    }

    protected String tableToCamelCase(String value) {
        if (value == null)
            return null;
        String[] parts = value.split("_");
        String camelCaseString = "";
        for (String part : parts) {
            if (camelCaseString.length() == 0)
                if (part.length() > 1)
                    camelCaseString = camelCaseString + part.substring(0, 1).toUpperCase()
                            + part.substring(1).toLowerCase();
                else
                    camelCaseString = camelCaseString + part.toUpperCase();
            else if (part.length() == 1)
                camelCaseString = camelCaseString + part.toUpperCase();
            else if (part.length() > 1)
                camelCaseString = camelCaseString + part.substring(0, 1).toUpperCase()
                        + part.substring(1).toLowerCase();
        }
        return camelCaseString + suffix;
    }

    protected String columnToCamelCase(String value) {
        if (value == null)
            return null;
        String[] parts = value.split("_");
        String camelCaseString = "";
        for (String part : parts) {
            if (camelCaseString.length() == 0)
                camelCaseString = camelCaseString + part.toLowerCase();
            else if (part.length() == 1)
                camelCaseString = camelCaseString + part.toUpperCase();
            else if (part.length() > 1)
                camelCaseString = camelCaseString + part.substring(0, 1).toUpperCase()
                        + part.substring(1).toLowerCase();
        }
        return camelCaseString;
    }

    protected String columnToDbConv(String value) {
        if (value == null)
            return null;
        String result = "";
        int last = 0;
        for (int i = 0, l = value.length(); i < l; i++) {
            if (Character.isUpperCase(value.charAt(i))) {
                result = result + value.substring(last, i).toUpperCase() + "_";
                last = i;
            }
        }
        if (last < value.length())
            result = result + value.substring(last).toUpperCase();
        return result;
    }

    protected PojoAttribute convertDbColumnDefinition(String dbName, PojoAttrType sqlType) {
        PojoAttribute attribute = new PojoAttribute();
        attribute.setName(columnToCamelCase(dbName));
        if (sqlType.getNativeType() != null) {
            attribute.setPrimitive(true);
            attribute.setClassName(sqlType.getNativeType().substring(1) + (sqlType.isArray() ? " []" : ""));
        } else if (sqlType.getRef() != null) {
            attribute.setPrimitive(false);
            attribute.setDependencyClassName(sqlType.getRef().getName());
            if (attribute.getDependencyClassName() == null) {
                attribute.setDependencyClassName(sqlType.getText());
            }
        } else {
            attribute.setPrimitive(false);
            attribute.setClassName(sqlType.getType().getIdentifier());
        }
        return attribute;
    }

    protected PojoAttribute convertDbColumnDefinition(String table, DbColumn dbColumn) {
        if (dbColumn == null)
            return null;
        PojoAttrType sqlType = columnTypes.containsKey(table) ? columnTypes.get(table).get(dbColumn.getName()) : null;
        if (sqlType == null)
            sqlType = tableTypes.containsKey(table) ? tableTypes.get(table)
                    .get(dbColumn.getType() + dbColumn.getSize()) : null;
        if (sqlType == null)
            sqlType = sqlTypes.get(dbColumn.getType() + dbColumn.getSize());
        if (sqlType == null)
            return null;
        PojoAttribute attribute = new PojoAttribute();
        attribute.setName(columnToCamelCase(dbColumn.getName()));
        attribute.setRequired(!dbColumn.isNullable());
        if (sqlType.getRef() != null) {
            attribute.setPrimitive(false);
            attribute.setDependencyClassName(sqlType.getRef().getName());
        } else if (sqlType.getNativeType() != null) {
            attribute.setPrimitive(true);
            attribute.setClassName(sqlType.getNativeType().substring(1) + (sqlType.isArray() ? " []" : ""));
        } else {
            attribute.setPrimitive(false);
            attribute.setClassName(sqlType.getType().getIdentifier());
        }
        return attribute;
    }

    protected PojoAttribute convertDbColumnDefault(String table, DbColumn dbColumn) {
        if (dbColumn == null)
            return null;
        PojoAttribute attribute = new PojoAttribute();
        attribute.setName(columnToCamelCase(dbColumn.getName()));
        attribute.setRequired(!dbColumn.isNullable());
        switch (dbColumn.getSqlType()) {
        case Types.BIT:
        case Types.BOOLEAN:
            if (dbColumn.isNullable() || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(Boolean.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.BOOLEAN.getName());
            }
            break;
        case Types.TINYINT:
            if (dbColumn.isNullable() || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(Byte.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.BYTE.getName());
            }
            break;
        case Types.SMALLINT:
            if (dbColumn.isNullable() || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(Short.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.SHORT.getName());
            }
            break;
        case Types.INTEGER:
            if (dbColumn.isNullable() || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(Integer.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.INT.getName());
            }
            break;
        case Types.BIGINT:
            if (dbColumn.isNullable() || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(Long.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.LONG.getName());
            }
            break;
        case Types.FLOAT:
        case Types.REAL:
            if (dbColumn.isNullable() || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(Float.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.FLOAT.getName());
            }
            break;
        case Types.DOUBLE:
            if (dbColumn.isNullable() || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(Double.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.DOUBLE.getName());
            }
            break;
        case Types.NUMERIC:
            attribute.setPrimitive(false);
            attribute.setClassName(BigDecimal.class.getName());
            break;
        case Types.DECIMAL:
            attribute.setPrimitive(false);
            if (dbColumn.getSize() < 3)
                attribute.setClassName(Byte.class.getName());
            else if (dbColumn.getSize() < 5)
                attribute.setClassName(Short.class.getName());
            else if (dbColumn.getSize() < 10)
                attribute.setClassName(Integer.class.getName());
            else if (dbColumn.getSize() < 20)
                attribute.setClassName(Long.class.getName());
            else
                attribute.setClassName(BigInteger.class.getName());
            break;
        case Types.CHAR:
        case Types.NCHAR:
            if (dbColumn.isNullable() || dbColumn.getSize() > 1 || doGenerateWrappers) {
                attribute.setPrimitive(false);
                attribute.setClassName(String.class.getName());
            } else {
                attribute.setPrimitive(true);
                attribute.setClassName(PrimitiveType.CHAR.getName());
            }
            break;
        case Types.VARCHAR:
        case Types.LONGNVARCHAR:
        case Types.NVARCHAR:
            attribute.setPrimitive(false);
            attribute.setClassName(String.class.getName());
            break;
        case Types.DATE:
            // java.util.Date or java.sql.Date ?
            attribute.setPrimitive(false);
            attribute.setClassName(java.util.Date.class.getName());
            break;
        case Types.TIME:
            attribute.setPrimitive(false);
            attribute.setClassName(java.sql.Time.class.getName());
            break;
        case Types.TIMESTAMP:
            // java.util.Date or java.sql.Timestamp ?
            attribute.setPrimitive(false);
            attribute.setClassName(java.sql.Timestamp.class.getName());
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.BLOB:
            attribute.setPrimitive(true);
            attribute.setClassName(PrimitiveType.BYTE_ARRAY.getName());
            break;
        case Types.CLOB:
            attribute.setPrimitive(false);
            attribute.setClassName(String.class.getName());
            break;
        default:
            // todo what type?
            attribute.setPrimitive(false);
            if (dbColumn.getType().indexOf("TIMESTAMP") == 0 || dbColumn.getType().indexOf("timestamp") == 0)
                attribute.setClassName(java.sql.Timestamp.class.getName());
            else
                attribute.setClassName("java.lang.Object");
        }
        return attribute;
    }
}
