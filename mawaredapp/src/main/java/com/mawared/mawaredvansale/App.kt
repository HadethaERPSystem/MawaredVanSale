package com.mawared.mawaredvansale

//import com.mazenrashed.printooth.Printooth
import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.mawared.mawaredvansale.controller.auth.AuthViewModelFactory
import com.mawared.mawaredvansale.controller.callcycle.cyentry.CallCycleEntryViewModelFactory
import com.mawared.mawaredvansale.controller.callcycle.cylist.CallCycleViewModelFactory
import com.mawared.mawaredvansale.controller.fms.payables.payableentry.PayableEntryViewModelFactory
import com.mawared.mawaredvansale.controller.fms.payables.payablelist.PayableViewModelFactory
import com.mawared.mawaredvansale.controller.fms.receivables.receivableentry.ReceivableEntryViewModelFactory
import com.mawared.mawaredvansale.controller.fms.receivables.receivablelist.ReceivableViewModelFactory
import com.mawared.mawaredvansale.controller.home.HomeViewModelFactory
import com.mawared.mawaredvansale.controller.home.dashboard.DashboardViewModelFactory
import com.mawared.mawaredvansale.controller.home.reportsdashboard.ReportViewModelFactory
import com.mawared.mawaredvansale.controller.map.MapViewModelFactory
import com.mawared.mawaredvansale.controller.marketplace.MarketPlaceViewModelFactory
import com.mawared.mawaredvansale.controller.marketplace.brand.BrandViewModelFactory
import com.mawared.mawaredvansale.controller.marketplace.cart.CartViewModelFactory
import com.mawared.mawaredvansale.controller.marketplace.category.CategoryViewModelFactory
import com.mawared.mawaredvansale.controller.marketplace.items.ItemsViewModelFactory
import com.mawared.mawaredvansale.controller.marketplace.offers.OffersViewModelFactory
import com.mawared.mawaredvansale.controller.marketplace.schedulecustomer.SelectCustomerViewModelFactory
import com.mawared.mawaredvansale.controller.md.customerentry.CustomerEntryViewModelFactory
import com.mawared.mawaredvansale.controller.md.customerlist.CustomerViewModelFactory
import com.mawared.mawaredvansale.controller.mnt.entry.MntEntryViewModelFactory
import com.mawared.mawaredvansale.controller.mnt.mntlist.MntsVieModelFactory
import com.mawared.mawaredvansale.controller.reports.customer.CustomerStatementViewModelFactory
import com.mawared.mawaredvansale.controller.reports.fms.CashbookStatementViewModelFactory
import com.mawared.mawaredvansale.controller.reports.kpi.KpiViewModelFactory
import com.mawared.mawaredvansale.controller.reports.sales.SalesStatementViewModelFactory
import com.mawared.mawaredvansale.controller.reports.stock.StockViewModelFactory
import com.mawared.mawaredvansale.controller.sales.delivery.deliveryentry.DeliveryEntryViewModelFactory
import com.mawared.mawaredvansale.controller.sales.delivery.deliverylist.DeliveryViewModelFactory
import com.mawared.mawaredvansale.controller.sales.invoices.addinvoice.AddInvoiceViewModelFactory
import com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist.InvoicesViewModelFactory
import com.mawared.mawaredvansale.controller.sales.order.addorder.AddOrderViewModelFactory
import com.mawared.mawaredvansale.controller.sales.order.orderslist.OrdersViewModelFactory
import com.mawared.mawaredvansale.controller.sales.psorder.psorderentry.PSOrderEntryViewModelFactory
import com.mawared.mawaredvansale.controller.sales.psorder.psorderlist.PSOrdersViewModelFactory
import com.mawared.mawaredvansale.controller.sales.salereturn.salereturnentry.SaleReturnEntryViewModelFactory
import com.mawared.mawaredvansale.controller.sales.salereturn.salereturnlist.SaleReturnViewModelFactory
import com.mawared.mawaredvansale.controller.settings.DownloadViewModelFactory
import com.mawared.mawaredvansale.controller.settings.SettingsViewModelFactory
import com.mawared.mawaredvansale.controller.surveyentry.SurveyEntryViewModelFactory
import com.mawared.mawaredvansale.controller.transfer.transferentry.TransferEntryViewModelFactory
import com.mawared.mawaredvansale.controller.transfer.transferlist.TransferViewModelFactory
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.ConnectivityInterceptor
import com.mawared.mawaredvansale.services.repositories.MenuRepository
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.services.repositories.UserRepository
import com.mawared.mawaredvansale.services.repositories.callcycle.CallCycleRepositoryImp
import com.mawared.mawaredvansale.services.repositories.callcycle.ICallCycleRepository
import com.mawared.mawaredvansale.services.repositories.delivery.DeliveryRepositoryImp
import com.mawared.mawaredvansale.services.repositories.delivery.IDeliveryRepository
import com.mawared.mawaredvansale.services.repositories.fms.IPayableRepository
import com.mawared.mawaredvansale.services.repositories.fms.IReceivableRepository
import com.mawared.mawaredvansale.services.repositories.fms.PayableRepositoryImp
import com.mawared.mawaredvansale.services.repositories.fms.ReceiableRepositoryImp
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.invoices.InvoiceRepositoryImp
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.MDataRepositoryImp
import com.mawared.mawaredvansale.services.repositories.md.DownloadRepository
import com.mawared.mawaredvansale.services.repositories.md.MasterDataRepository
import com.mawared.mawaredvansale.services.repositories.mnt.IMaintenanceRepository
import com.mawared.mawaredvansale.services.repositories.mnt.MaintenanceRepositoryImp
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository
import com.mawared.mawaredvansale.services.repositories.order.OrderRepositoryImp
import com.mawared.mawaredvansale.services.repositories.reports.customer.CuStatementRepositoryImp
import com.mawared.mawaredvansale.services.repositories.reports.customer.ICuStatementRepository
import com.mawared.mawaredvansale.services.repositories.reports.dashboard.DashboardRepositoryImp
import com.mawared.mawaredvansale.services.repositories.reports.dashboard.IDashboardRepository
import com.mawared.mawaredvansale.services.repositories.reports.fms.CashbookRepositoryImp
import com.mawared.mawaredvansale.services.repositories.reports.fms.ICashbookRepository
import com.mawared.mawaredvansale.services.repositories.reports.sales.ISalesRepository
import com.mawared.mawaredvansale.services.repositories.reports.sales.SalesRepositoryImp
import com.mawared.mawaredvansale.services.repositories.reports.stock.IStockRepository
import com.mawared.mawaredvansale.services.repositories.reports.stock.StockRepositoryImp
import com.mawared.mawaredvansale.services.repositories.salereturn.ISaleReturnRepository
import com.mawared.mawaredvansale.services.repositories.salereturn.SaleReturnRepositoryImp
import com.mawared.mawaredvansale.services.repositories.srv.SurveyRepositoryImp
import com.mawared.mawaredvansale.services.repositories.transfer.ITransferRepository
import com.mawared.mawaredvansale.services.repositories.transfer.TransferRepositoryImp
import com.mawared.mawaredvansale.utilities.SharedPrefs
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton


/*
 * Created by alibawi 2019-07-05
 */

// this class run before any class like startup in c#
class App : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@App))
        // singleton means single instance for class not multiple
        bind() from singleton { ConnectivityInterceptor(instance()) }
        bind() from singleton { ApiService(instance()) }
        bind() from singleton { AppDatabase(instance()) }

        //bind<INetworkDataSource>() with  singleton { NetworkDataSourceImp(instance()) }

        bind() from singleton { UserRepository(instance(), instance()) }
        bind() from singleton { OrderRepository(instance()) }
        bind() from singleton { MenuRepository(instance(), instance()) }

        bind() from singleton { MasterDataRepository(instance(), instance()) }

        bind() from singleton { DownloadRepository(instance(), instance()) }

        bind<IMDataRepository>() with singleton { MDataRepositoryImp(instance(), instance()) }

        bind<IInvoiceRepository>() with singleton { InvoiceRepositoryImp(instance()) }

        bind<IOrderRepository>() with singleton { OrderRepositoryImp(instance()) }

        bind<ISaleReturnRepository>() with singleton {SaleReturnRepositoryImp(instance()) }

        bind<IReceivableRepository>() with singleton { ReceiableRepositoryImp(instance()) }

        bind<IPayableRepository>() with singleton { PayableRepositoryImp(instance()) }

        bind<IDeliveryRepository>() with singleton { DeliveryRepositoryImp(instance()) }

        bind<ITransferRepository>() with singleton { TransferRepositoryImp(instance())}
        bind<ICallCycleRepository>() with singleton { CallCycleRepositoryImp(instance())}
        // Fror Reporting
        bind<ICuStatementRepository>() with singleton { CuStatementRepositoryImp(instance()) }
        bind<ICashbookRepository>() with singleton { CashbookRepositoryImp(instance()) }
        bind<ISalesRepository>() with singleton { SalesRepositoryImp(instance()) }
        bind<IStockRepository>() with singleton { StockRepositoryImp(instance()) }
        bind<IDashboardRepository>() with singleton { DashboardRepositoryImp(instance()) }
        bind<IMaintenanceRepository>() with singleton { MaintenanceRepositoryImp(instance())}

        bind() from singleton { SurveyRepositoryImp(instance()) }
        // no singleton for below because we need more than one instance for view model
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { HomeViewModelFactory(instance()) }
        bind() from provider { DashboardViewModelFactory(instance(), instance()) }
        bind() from provider { ReportViewModelFactory(instance()) }
        // invoice view model factory
        bind() from provider { InvoicesViewModelFactory(instance()) }
        bind() from provider { AddInvoiceViewModelFactory(instance(), instance()) }

        // Sale order view model factory
        bind() from provider { OrdersViewModelFactory(instance()) }
        bind() from provider { AddOrderViewModelFactory(instance(), instance()) }

        // Pre-Sale order
        bind() from provider { PSOrdersViewModelFactory(instance()) }
        bind() from provider { PSOrderEntryViewModelFactory(instance(), instance()) }

        // Sale return view model factory
        bind() from provider { SaleReturnViewModelFactory(instance()) }
        bind() from provider { SaleReturnEntryViewModelFactory(instance(), instance()) }

        bind() from provider { DownloadViewModelFactory(instance()) }

        // Receivable view model factory
        bind() from provider { ReceivableViewModelFactory(instance()) }
        bind() from provider { ReceivableEntryViewModelFactory(instance(), instance()) }

        // Payable view model factory
        bind() from provider { PayableViewModelFactory(instance()) }
        bind() from provider { PayableEntryViewModelFactory(instance(), instance()) }

        // Customer view model factory
        bind() from provider { CustomerViewModelFactory(instance()) }
        bind() from provider { CustomerEntryViewModelFactory(instance()) }

        // Delivery factory
        bind() from provider { DeliveryViewModelFactory(instance()) }
        bind() from provider { DeliveryEntryViewModelFactory(instance(), instance()) }

        // Survey Entry
        bind() from provider { SurveyEntryViewModelFactory(instance(), instance()) }
        // Transfer
        bind() from provider { TransferViewModelFactory(instance())}
        bind() from provider { TransferEntryViewModelFactory(instance(), instance()) }
        // Call Cycle
        bind() from provider { CallCycleViewModelFactory(instance(), instance())}
        bind() from provider { CallCycleEntryViewModelFactory(instance(), instance())}

        // Customer statement report
        bind() from provider { CustomerStatementViewModelFactory(instance(), instance()) }
        // Cashbook statement report
        bind() from provider { CashbookStatementViewModelFactory(instance()) }
        bind() from provider { MapViewModelFactory(instance()) }
        // Maintenance
        bind() from provider { MntsVieModelFactory(instance()) }
        bind() from provider { MntEntryViewModelFactory(instance(), instance()) }
        // Sales statement report
        bind() from provider { SalesStatementViewModelFactory(instance()) }
        // Stock statement report
        bind() from provider { StockViewModelFactory(instance()) }

        bind() from provider { SettingsViewModelFactory(instance()) }

        bind() from provider { KpiViewModelFactory(instance(), instance()) }

        bind() from provider { ItemsViewModelFactory(instance(), instance()) }
        bind() from provider { OffersViewModelFactory(instance(), instance()) }
        bind() from provider { CategoryViewModelFactory(instance()) }
        bind() from provider { BrandViewModelFactory(instance()) }
        bind() from provider { MarketPlaceViewModelFactory(instance()) }
        bind() from provider { CartViewModelFactory(instance(), instance(), instance(), instance()) }
        bind() from provider { SelectCustomerViewModelFactory(instance(), instance()) }
    }

    companion object{
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        AndroidThreeTen.init(this)
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
        //Printooth.init(this)
    }
}