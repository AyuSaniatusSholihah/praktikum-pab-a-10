# DIAGRAM KELAS (CLASS DIAGRAM): **SEWAIN**

Dokumen ini memuat Diagram Kelas (Class Diagram) yang menggambarkan struktur class utama di aplikasi Android **Sewain**, berfokus pada hubungan arsitektural MVVM (Model-View-ViewModel), Repository, Utility, dan Jaringan (Retrofit).

---

## 1. Diagram Kelas (Mermaid)

```mermaid
classDiagram
    %% --- LAYER UI / VIEW ---
    class MainActivity {
        -initialScreen: ScreenTarget
        +onCreate(savedInstanceState: Bundle)
        +onNewIntent(intent: Intent)
        -getScreenFromStr(str: String): Screen
    }
    
    class ScreenTarget {
        +screen: Screen
        +id: Long
        +productId: Int?
    }
    
    class Screen {
        <<enumeration>>
        Home
        Rental
        Keranjang
        MyKatalog
        Profile
        RiwayatTransaksi
        AddItem
        EditItem
        DetailTransaksi
        Pembayaran
        MyRental
        RentalsOwner
        CheckoutPayment
        Confirm
        MyWallet
        Settings
    }

    %% --- LAYER VIEWMODEL ---
    class TransaksiViewModel {
        -repository: TransaksiRepository
        -_checkoutState: MutableLiveData
        +checkoutState: LiveData
        -_pembayaranState: MutableLiveData
        +pembayaranState: LiveData
        -_transaksiList: MutableLiveData
        +transaksiList: LiveData
        +checkout(token: String, ids: List~Int~)
        +bayar(token: String, request: BayarRequest)
        +kembalikanBarang(token: String, id: Int, foto: Part, rating: RequestBody)
        +verifikasiPengembalian(token: String, id: Int, request: VerifikasiRequest)
    }

    class KatalogViewModel {
        -repository: KatalogRepository
        -_katalogPublik: MutableLiveData
        +katalogPublik: LiveData
        +getKatalogPublik(search: String?, kategoriId: Int?)
        +createKatalog(token: String, request: Multipart)
        +updateKatalog(token: String, id: Int, request: Multipart)
    }

    class KeranjangViewModel {
        -repository: KeranjangRepository
        -_keranjang: MutableLiveData
        +keranjang: LiveData
        +getKeranjang(token: String)
        +addToKeranjang(token: String, barangId: Int, jumlah: Int)
        +removeKeranjangItem(token: String, id: Int)
    }

    %% --- LAYER REPOSITORY ---
    class TransaksiRepository {
        -apiService: ApiService
        +getRiwayatTransaksi(token: String) Flow~Resource~
        +checkout(token: String, ids: List~Int~) Flow~Resource~
        +bayar(token: String, request: BayarRequest) Flow~Resource~
        +kembalikanBarang(token: String, id: Int, ...) Flow~Resource~
    }

    class KatalogRepository {
        -apiService: ApiService
        +getKatalogPublik(...) Flow~Resource~
        +createKatalog(...) Flow~Resource~
        +deleteKatalog(...) Flow~Resource~
    }

    class KeranjangRepository {
        -apiService: ApiService
        +getKeranjang(token: String) Flow~Resource~
        +addToKeranjang(...) Flow~Resource~
        +removeKeranjangItem(...) Flow~Resource~
    }

    %% --- LAYER UTILS & SESSION ---
    class SessionManager {
        -prefs: SharedPreferences
        -USER_TOKEN: String
        -USER_ROLE: String
        +saveToken(token: String)
        +getToken() String
        +saveRole(role: String)
        +getRole() String
        +isLoggedIn() Boolean
        +clearSession()
    }

    class Resource {
        <<sealed class>>
        Success
        Error
        Loading
    }

    %% --- LAYER NETWORK ---
    class ApiClient {
        -IP_LAPTOP: String
        -BASE_URL: String
        +IMAGE_BASE_URL: String
        +instance: ApiService
    }

    class ApiService {
        <<interface>>
        +login(request: LoginRequest) Response
        +register(request: RegisterRequest) Response
        +getKatalogPublik(...) Response
        +checkout(token: String, request: CheckoutRequest) Response
        +bayar(token: String, request: BayarRequest) Response
        +kembalikanBarang(...) Response
        +verifikasiPengembalian(...) Response
    }

    %% --- HUBUNGAN CLASS (RELATIONSHIPS) ---
    MainActivity --> ScreenTarget : menggunakan
    ScreenTarget --> Screen : mendefinisikan
    MainActivity --> SessionManager : mengelola auth session
    
    MainActivity --> TransaksiViewModel : berinteraksi
    MainActivity --> KatalogViewModel : berinteraksi
    MainActivity --> KeranjangViewModel : berinteraksi
    
    TransaksiViewModel --> TransaksiRepository : bergantung pada
    KatalogViewModel --> KatalogRepository : bergantung pada
    KeranjangViewModel --> KeranjangRepository : bergantung pada
    
    TransaksiRepository --> ApiClient : meminta instansi
    KatalogRepository --> ApiClient : meminta instansi
    KeranjangRepository --> ApiClient : meminta instansi
    
    ApiClient --> ApiService : membungkus instansi
    
    TransaksiRepository ..> Resource : mengembalikan flow data
    KatalogRepository ..> Resource : mengembalikan flow data
    KeranjangRepository ..> Resource : mengembalikan flow data
```

---

## 2. Keterangan Hubungan Antar Komponen

1.  **View (MainActivity) ke ViewModel**: `MainActivity` bertindak sebagai container utama yang menginisialisasi serta memanggil aksi dari `TransaksiViewModel`, `KatalogViewModel`, dan `KeranjangViewModel` berdasarkan halaman (*Screen*) Jetpack Compose yang sedang aktif.
2.  **ViewModel ke Repository**: ViewModel bertugas menampung state UI. ViewModel memanggil fungsi repository di dalam `viewModelScope` (Coroutine) untuk memproses data secara asinkron.
3.  **Repository ke ApiClient / ApiService**: Repository memanggil API backend Laravel melalui `ApiClient.instance` yang bertipe interface `ApiService` (Retrofit).
4.  **Resource Wrapper**: Repository membungkus hasil respons HTTP (`Retrofit.Response`) ke dalam kelas *sealed* `Resource` (bisa bertipe `Success`, `Error`, atau `Loading`), memancarkannya (`emit`) melalui Kotlin Flow ke ViewModel, lalu diamati oleh UI Compose untuk penanganan status loading dan error secara reaktif.
5.  **SessionManager**: Menggunakan `SharedPreferences` Android secara independen untuk persistensi token JWT (`Bearer token`) dan otorisasi role pengguna, yang dibaca oleh `MainActivity` dan dikirim sebagai header `Authorization` pada pemanggilan API.
