package com.mawared.mawaredvansale.utilities

/*
 * Created by alibawi on 2019-07-02
 */

//const val BASE_URL_API = "http://172.0.0.1:80/mderp.WebAPI/api/"
//const val BASE_URL_API = "http://172.29.50.2:6615/api/"
////
//const val BASE_URL = "http://172.29.50.2:6615/"

const val BASE_URL_API = "http://10.200.0.10/mderp.WebAPI/api/"
const val BASE_URL = "http://10.200.0.10/mderp.WebAPI/"

//const val BASE_URL_API = "http://192.168.1.191/mderp.WebAPI/api/"
//const val BASE_URL = "http://192.168.1.191/mderp.WebAPI/api/"

const val URL_LOGIN = "${BASE_URL_API}PDAUserAuth/login"

//Master Data Url
// Get All Product Url
const val URL_ALL_PRODUCTS = "${BASE_URL_API}PDAMasterData/ProductGetByTerm"

// Get All Product Url
const val URL_PRODUCT_BY_BARCODE = "${BASE_URL_API}PDAMasterData/Product_GetByBarcode"

// Get All Brand for all products Url
const val URL_ALL_PRODUCTS_BRAND = "${BASE_URL_API}PDAMasterData/Product_BrandGetByTerm"
// Get all product categories URL
const val URL_ALL_PRODUCTS_CATEGORY = "${BASE_URL_API}PDAMasterData/Product_CategoryGetByTerm"

// Get all products price list URL
const val URL_PRODUCT_PRICE = "${BASE_URL_API}PDAMasterData/Product_Price_ListGetByProductId"
// Get all products price list URL
const val URL_ALL_PRODUCTS_PRICE_LIST = "${BASE_URL_API}PDAMasterData/Product_Price_ListGetAll"

// Voucher By Code
const val URL_VOUCHER_BY_CODE = "${BASE_URL_API}PDAMasterData/Voucher_GetByCode"

// Get all regions for specific salesman URL
const val URL_ALL_REGION = "${BASE_URL_API}PDAMasterData/RegionGetByTerm"

// CURRENCY URL
const val URL_ALL_CURRENCIES = "${BASE_URL_API}PDAMasterData/CurrencyGetAll"
const val URL_CURRENCY_BY_CLIENT_ID = "${BASE_URL_API}PDAMasterData/CurrencyGetByClientId"
const val URL_CURRENCY_BY_CODE = "${BASE_URL_API}PDAMasterData/CurrencyGetByCode"
// Get latest exchange rate for all currencies URL
const val URL_ALL_CURRENCIES_RATE = "${BASE_URL_API}PDAMasterData/Currency_RateGetByTerm"
const val URL_CURRENT_CURRENCY_RATE = "${BASE_URL_API}PDAMasterData/Currency_RateLatest"

// Get all customers for specific salesman URL
const val URL_ALL_CUSTOMERS = "${BASE_URL_API}PDAMasterData/Customers_BySalesmanId"
const val URL_CUSTOMER_BY_Id = "${BASE_URL_API}PDAMasterData/Customer_GetById"
const val URL_INSERT_CUSTOMER = "${BASE_URL_API}PDAMasterData/Customer_SaveOrUpdate"

const val URL_ALL_CUSTOMER_GROUP = "${BASE_URL_API}PDAMasterData/CG_GetByTerm"
const val URL_ALL_CPT = "${BASE_URL_API}PDAMasterData/CPT_GetByTerm"

// Get salesman for this PDA URL
const val URL_ALL_SALESMAN_BY_CODE = "${BASE_URL_API}PDAMasterData/Salesman_GetByCode"

const val URL_ALL_SALESMAN_CUSTOMERS = "${BASE_URL_API}PDAMasterData/Salesman_CustomerGetByTerm"
// Warehouse URL
const val URL_WAREHOUSE_GET_ALL = "${BASE_URL_API}PDAMasterData/Warehouse_GetAll"
// Get delivery URL
const val URL_DELIVERY_UPDATE = "${BASE_URL_API}Delivery/Delivery_SaveOrUpdate"
const val URL_DELIVERY_BY_ID = "${BASE_URL_API}Delivery/Delivery_GetById"
const val URL_ALL_DELIVERY_BY_SALESMANID = "${BASE_URL_API}Delivery/Delivery_GetBySalesmanId"

const val URL_ALL_DELIVERY_DETAILS = "${BASE_URL_API}Delivery/GetItems_ByDeliveryId"

// check load URL
const val URL_ALL_CHECK_LOAD = "${BASE_URL_API}Check_Load/GetBySalesmanId"
const val URL_ALL_CHECK_LOAD_DETAILS = "${BASE_URL_API}Check_Load/GetDetailBySalesmanId"

// Sales URL
const val URL_ADD_SALE = "${BASE_URL_API}Sale/AddInvoice"
const val URL_SALE_BY_ID = "${BASE_URL_API}Sale/Invoice_GetById"
const val URL_SALES_FOR_SALESMAN = "${BASE_URL_API}Sale/Invoice_GetBySalesmanId"
const val URL_SALES_DELETE = "${BASE_URL_API}Sale/Invoice_DeleteById"

const val URL_SALES_ITEMS = "${BASE_URL_API}Sale/SaleItem_GetByMasterId"

// Sale Return URL
const val URL_ADD_SALE_RETURN = "${BASE_URL_API}Sale_Return/AddSaleReturn"
const val URL_SALE_RETURN_BY_ID = "${BASE_URL_API}Sale_Return/Return_GetById"
const val URL_SALE_RETURN_FOR_SALESMAN = "${BASE_URL_API}Sale_Return/Return_GetByReturns"
const val URL_SALE_RETURN_DELETE = "${BASE_URL_API}Sale_Return/Return_DeleteById"

const val URL_SALE_RETURN_ITEMS = "${BASE_URL_API}Sale_Return/Return_GetByReturnId"


// Payable
const val URL_ADD_PAYABLE = "${BASE_URL_API}PDAPayable/Payable_SaveOrUpdate"
const val URL_PAYABLE_BY_ID = "${BASE_URL_API}PDAPayable/Payable_GetById"
const val URL_PAYABLE_FOR_CUSTOMERS = "${BASE_URL_API}PDAPayable/Payable_GetBySalesmanId"
const val URL_PAYABLE_DELETE = "${BASE_URL_API}PDAPayable/Payable_DeleteById"
// Receivable
const val URL_ADD_RECEIVABLE = "${BASE_URL_API}PDAReceivable/Receivable_SaveOrUpdate"
const val URL_RECEIVABLE_BY_ID = "${BASE_URL_API}PDAReceivable/Receivable_GetById"
const val URL_RECEIVABLE_FOR_CUSTOMERS = "${BASE_URL_API}PDAReceivable/Receivable_GetBySalesmanId"
const val URL_RECEIVABLE_DELETE = "${BASE_URL_API}PDAReceivable/Receivable_DeleteById"

// ORDER URL
const val URL_ADD_ORDER = "${BASE_URL_API}Sale_Order/AddOrder"
const val URL_UPDATE_ORDER = "${BASE_URL_API}Sale_Order/Update"
const val URL_ORDER_BY_ID = "${BASE_URL_API}Sale_Order/GetById"
const val URL_ORDER_BY_CUSTOMERS = "${BASE_URL_API}Sale_Order/GetByCustomerId"
const val URL_ORDER_BY_SALESMAN = "${BASE_URL_API}Sale_Order/GetBySalesmanId"
const val URL_ORDER_DELETE = "${BASE_URL_API}Sale_Order/Order_DeleteById"

const val URL_ORDER_ITEMS = "${BASE_URL_API}Sale_Order/GetByMasterId"

// INVENTORY STOCK-IN URL
const val URL_ADD_STOCK_IN = "${BASE_URL_API}StockIn/Insert"
const val URL_UPDATE_STOCK_IN = "${BASE_URL_API}StockIn/Update"
const val URL_STOCK_IN_BY_ID = "${BASE_URL_API}StockIn/GetById"
const val URL_STOCK_IN_BY_CUSTOMERS = "${BASE_URL_API}StockIn/GetByCustomerId"
const val URL_STOCK_IN_ITEMS = "${BASE_URL_API}StockIn/GetByMasterId"

// INVENTORY STOCK-OUT URL
const val URL_ADD_STOCK_OUT = "${BASE_URL_API}StockOut/Insert"
const val URL_UPDATE_STOCK_OUT = "${BASE_URL_API}StockOut/Update"
const val URL_STOCK_OUT_BY_ID = "${BASE_URL_API}StockOut/GetById"
const val URL_STOCK_OUT_BY_CUSTOMERS = "${BASE_URL_API}StockOut/GetByCustomerId"
const val URL_STOCK_OUT_ITEMS = "${BASE_URL_API}StockOut/GetByMasterId"

// Transfer URL
const val URL_ADD_TRANSFER = "${BASE_URL_API}Transfer/transfer_SaveOrUpdate"
const val URL_TRANSFER_GET_BY_User = "${BASE_URL_API}Transfer/transfer_getByUserId"
const val URL_TRANSFER_GET_BY_ID = "${BASE_URL_API}Transfer/transfer_getById"
const val URL_TRANSFER_GET_BY_MASTER_ID = "${BASE_URL_API}Transfer/transfer_GetItemsByMasterId"

// Voucher get by code
const val URL_ADD_VOUCHER_BYCODE = "${BASE_URL_API}PDAMasterData/Voucher_GetByCode"

// Broadcast Constants
const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"

// Exception Messages
const val NO_CONNECTION_AVAILABLE = "Make sure you have an active data connection"

// Image URL
const val URL_IMAGE = "${BASE_URL}uploads/Products/"
const val URL_LOGO = "${BASE_URL}CompanyInfo/"

const val EXTRA_DATA_LOCATIONS = "extra_data_locations"
const val EXTRA_CURRENT_LOCATION = "extra_current_location"
const val EXTRA_DEVICE_ADDRESS = "device_address"
