// package managers;
//
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
//
// import adt.models.Category;
// import adt.models.Company;
// import adt.models.Sheet;
// import common.Result;
// import common.Result.FailResult;
// import common.Result.SuccessResult;
// import misc.Utils;
// import servlets.ServletLog;
//
// public class DataManager {
//
// private static String selectedQuarter = null;
// private static String selectedYear = null;
//
// /**
// * @return the selectedTerm
// */
// public static String getSelectedTerm() {
//
// if (selectedQuarter == null || selectedYear == null) {
// getSelectedTermFromDB();
// }
// return selectedYear + "_" + selectedQuarter;
// }
//
// /**
// * @return the selected quarter, in proper/camel case.
// */
// public static String getSelectedQuarter() {
//
// if (selectedQuarter == null || selectedYear == null) {
// getSelectedTermFromDB();
// }
// return Utils.toProperCase(selectedQuarter);
// }
//
// /**
// * @return the selected year, in proper/camel case.
// */
// public static String getSelectedYear() {
//
// if (selectedQuarter == null || selectedYear == null) {
// getSelectedTermFromDB();
// }
// return selectedYear;
// }
//
// /**
// * @param selectedTerm
// * the selectedTerm to set
// */
// public static void setSelectedTerm(String selectedYear, String
// selectedQuarter) {
//
// DataManager.selectedQuarter = selectedQuarter;
// DataManager.selectedYear = selectedYear;
//
// try {
// PreparedStatement updateStatement = SQLManager.getConn().prepareStatement(
// "INSERT INTO Vars (item, value, type) VALUES (?, ?, ?), (?, ?, ?) ON
// DUPLICATE KEY UPDATE item=values(item), value=values(value),
// type=values(type);");
// updateStatement.setString(1, "selectedQuarter");
// updateStatement.setString(2, selectedQuarter);
// updateStatement.setString(3, "selectedTerm");
// updateStatement.setString(4, "selectedYear");
// updateStatement.setString(5, selectedYear);
// updateStatement.setString(6, "selectedTerm");
// updateStatement.executeUpdate();
// } catch (Exception e) {
// ServletLog.logEvent(e);
// }
// }
//
// private static void getSelectedTermFromDB() {
//
// try {
// ResultSet r = SQLManager.getConn().createStatement()
// .executeQuery("SELECT item, value, type FROM Vars WHERE type =
// 'selectedTerm';");
//
// while (r.next()) {
// if (r.getString("item").equalsIgnoreCase("selectedQuarter")) {
// selectedQuarter = Utils.toProperCase(r.getString("value"));
// } else if (r.getString("item").equalsIgnoreCase("selectedYear")) {
// selectedYear = r.getString("value");
// }
// }
//
// r.close();
//
// } catch (Exception e) {
// ServletLog.logEvent(e);
// }
// }
//
// public static boolean checkDBExists(String year, String quarter) throws
// SQLException, ClassNotFoundException {
//
// ResultSet rs = null;
//
// PreparedStatement checkDBExists = SQLManager.getConn()
// .prepareStatement("SELECT COUNT(*) AS DBCount FROM Terms WHERE year=? AND
// quarter=?;");
// checkDBExists.setString(1, year);
// checkDBExists.setString(2, Utils.toProperCase(quarter));
// rs = checkDBExists.executeQuery();
//
// if (rs.next() && rs.getInt("DBCount") > 0) {
// return true;
// }
// return false;
// }
//
// public static Result updateTermVars(String dbName, Sheet termVars) throws
// SQLException, ClassNotFoundException {
//
// PreparedStatement insertVars = SQLManager.getConn().prepareStatement(
// "INSERT INTO TermVars (item, value, type) VALUES (?, ?, ?) ON DUPLICATE KEY
// UPDATE value=values(value), type=values(type);");
//
// for (int i = 0; i < termVars.getRows(); i++) {
// insertVars.setString(1, termVars.getItem(i, "Item", String.class));
// insertVars.setString(2, termVars.getItem(i, "Value", String.class));
// insertVars.setString(3, termVars.getItem(i, "Type", String.class));
// insertVars.executeUpdate();
// }
//
// return new SuccessResult();
// }
//
// public static Result updateTableMappings(String dbName, Sheet tableMappings,
// List<Company> companies)
// throws SQLException, ClassNotFoundException {
//
// PreparedStatement insertTableMapping = SQLManager.getConn().prepareStatement(
// "INSERT INTO TableMappings (tableNumber, companyId, tableSize) VALUES (?, ?,
// ?) ON DUPLICATE KEY UPDATE companyId=values(companyId),
// tableSize=values(tableSize);");
// HashMap<Long, Long> tableCompanyMap = new HashMap<Long, Long>();
// for (Company c : companies) {
// // tableCompanyMap.put(c.getTableId(), c.getId());
// }
//
// for (int i = 0; i < tableMappings.getRows(); i++) {
// Long companyID = tableCompanyMap.get(tableMappings.getItem(i, "Table Number",
// Long.class));
// insertTableMapping.setInt(1, tableMappings.getItem(i, "Table Number",
// Integer.class));
// if (companyID != null) {
// insertTableMapping.setLong(2, companyID);
// } else {
// insertTableMapping.setNull(2, java.sql.Types.INTEGER);
// }
// insertTableMapping.setInt(3, tableMappings.getItem(i, "Table Size",
// Integer.class));
// insertTableMapping.executeUpdate();
// }
//
// return new SuccessResult();
// }
//
// public static Result updateCategories(String dbName, List<Category>
// categories)
// throws SQLException, ClassNotFoundException {
//
// PreparedStatement insertCategories = SQLManager.getConn().prepareStatement(
// "INSERT INTO Categories (id, name, type) VALUES (?, ?, ?) ON DUPLICATE KEY
// UPDATE name=values(name), type=values(type);");
//
// for (Category c : categories) {
// insertCategories.setLong(1, c.getId());
// insertCategories.setString(2, c.getName());
// insertCategories.setString(3, c.getType());
// insertCategories.executeUpdate();
// }
//
// return new SuccessResult();
// }
//
// public static Result updateCompanies(String dbName, List<Company> companies)
// throws SQLException, ClassNotFoundException {
//
// PreparedStatement insertCompanies = SQLManager.getConn().prepareStatement(
// "INSERT INTO Companies (id, name, description) VALUES (?, ?, ?) ON DUPLICATE
// KEY UPDATE name=values(name), description=values(description);");
//
// for (Company c : companies) {
// insertCompanies.setLong(1, c.getId());
// insertCompanies.setString(2, c.getName());
//
// if (c.getDescription() != null) {
// insertCompanies.setString(3, c.getDescription());
// } else {
// insertCompanies.setNull(3, java.sql.Types.BLOB);
// }
// insertCompanies.executeUpdate();
// }
//
// return new SuccessResult();
// }
//
// public static Result updateCategories_Companies(String dbName, List<Company>
// companies)
// throws SQLException, ClassNotFoundException {
//
// PreparedStatement insertCompanies = SQLManager.getConn()
// .prepareStatement("INSERT IGNORE INTO Categories_Companies (categoryId,
// companyId) VALUES (?, ?)");
//
// for (Company company : companies) {
// // for (Long categoryId : company.getCategories()) {
// // insertCompanies.setLong(1, categoryId);
// // insertCompanies.setLong(2, company.getId());
// // insertCompanies.executeUpdate();
// // }
// }
//
// return new SuccessResult();
// }
//
// public static Result updateCategoriesAndCompanies(String dbName, Sheet
// categories, Sheet companies)
// throws SQLException, ClassNotFoundException {
//
// HashMap<String, HashMap<String, Long>> categoryLookupTable = new
// HashMap<String, HashMap<String, Long>>();
// List<Company> companyList = new ArrayList<Company>();
// List<Category> categoryList = new ArrayList<Category>();
//
// for (int i = 0; i < categories.getRows(); i++) {
// String name = categories.getItem(i, "Name", String.class);
// String type = categories.getItem(i, "Type", String.class);
// Category newCategory = new Category((long) (i + 1), name, type);
//
// if (categoryLookupTable.get(type) == null) {
// categoryLookupTable.put(type, new HashMap<String, Long>());
// }
//
// categoryLookupTable.get(type).put(name, newCategory.getId());
//
// categoryList.add(newCategory);
// }
//
// for (int i = 0; i < companies.getRows(); i++) {
// String name = companies.getItem(i, "companyName", String.class).replace("\"",
// "");
//
// String[] majors = companies.getItem(i, "companyMajor", String.class) == null
// ? new String[] {}
// : companies.getItem(i, "companyMajor", String.class).split(",");
//
// String[] workAuths = companies.getItem(i, "companyWorkAuth", String.class) ==
// null ? new String[] {}
// : companies.getItem(i, "companyWorkAuth", String.class).split(",");
//
// String[] posTypes = companies.getItem(i, "companyPositionType", String.class)
// == null ? new String[] {}
// : companies.getItem(i, "companyPositionType", String.class).split(",");
//
// String description = companies.getItem(i, "companyDetailDescription",
// String.class).replace("\"", "");
//
// String websiteLink = companies.getItem(i, "companyDetailWebsite-href",
// String.class).replace("\"", "");
//
// String address = companies.getItem(i, "companyDetailAddress",
// String.class).replace("\"", "");
//
// ArrayList<Long> majorsList = new ArrayList<Long>();
// ArrayList<Long> workAuthList = new ArrayList<Long>();
// ArrayList<Long> posTypeList = new ArrayList<Long>();
//
// for (String major : majors) {
// Long id = categoryLookupTable.get("Major").get(major.trim());
// if (id != null) {
// majorsList.add(id);
// }
// }
//
// for (String workAuth : workAuths) {
// Long id = categoryLookupTable.get("Work Authorization").get(workAuth.trim());
// if (id != null) {
// workAuthList.add(id);
// }
// }
//
// for (String posType : posTypes) {
// Long id = categoryLookupTable.get("Position Type").get(posType.trim());
// if (id != null) {
// posTypeList.add(id);
// }
// }
//
// if (majorsList.isEmpty()) {
// majorsList.addAll(categoryLookupTable.get("Major").values());
// }
// if (workAuthList.isEmpty()) {
// workAuthList.addAll(categoryLookupTable.get("Work Authorization").values());
// }
// if (posTypeList.isEmpty()) {
// posTypeList.addAll(categoryLookupTable.get("Position Type").values());
// }
//
// List<Long> companyCategories = new ArrayList<Long>();
// companyCategories.addAll(majorsList);
// companyCategories.addAll(workAuthList);
// companyCategories.addAll(posTypeList);
// Long tableId = companies.getItem(i, "Table Number", Long.class);
//
// Company newCompany = new Company((long) (i + 100), name, null, websiteLink,
// address);
// companyList.add(newCompany);
// }
//
// Result updateCategoriesResponse = updateCategories(dbName, categoryList);
// Result updateCompaniesResponse = updateCompanies(dbName, companyList);
// Result updateCategories_CompaniesResponse =
// updateCategories_Companies(dbName, companyList);
//
// if (updateCategoriesResponse.isSuccess() &&
// updateCompaniesResponse.isSuccess()
// && updateCategories_CompaniesResponse.isSuccess()) {
// SuccessResult response = new SuccessResult();
// response.put("companyList", companyList);
// return response;
// }
//
// Result failed = new FailResult(-1);
// failed.put("updateCategoriesResponse", updateCompaniesResponse);
// failed.put("updateCompaniesResponse", updateCompaniesResponse);
// failed.put("updateCategories_CompaniesResponse",
// updateCategories_CompaniesResponse);
// return failed;
//
// }
// }
