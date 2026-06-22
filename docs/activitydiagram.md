# DIAGRAM AKTIVITAS (ACTIVITY DIAGRAM): **SEWAIN**

Dokumen ini memuat Diagram Aktivitas yang menggambarkan alur kerja (workflow) proses bisnis utama pada aplikasi **Sewain**, mulai dari proses penyewaan hingga pengembalian barang.

---

## 1. Alur Penyewaan Barang (Booking & Payment Workflow)

Diagram ini menunjukkan langkah-langkah yang dilakukan oleh Penyewa (Tenant) sejak mencari barang hingga menyelesaikan pembayaran menggunakan saldo dompet virtual.

```mermaid
stateDiagram-v2
    [*] --> CariBarang : Tenant mencari & memfilter barang sewaan
    CariBarang --> DetailBarang : Memilih barang untuk melihat detail
    DetailBarang --> MasukKeranjang : Klik "Tambah ke Keranjang"
    
    state Keranjang {
        [*] --> SetTanggal : Tentukan Rentang Tanggal Sewa
        SetTanggal --> SetJumlah : Tentukan Jumlah Barang (Stok)
        SetJumlah --> TinjauEstimasi : Sistem kalkulasi biaya & jaminan
    }
    
    TinjauEstimasi --> Checkout : Klik "Checkout" di Keranjang
    Checkout --> TinjauPesanan : Tinjau alamat & rincian biaya sewa
    TinjauPesanan --> BayarSewa : Klik "Bayar Sekarang"
    
    state ProsesPembayaran <<choice>>
    BayarSewa --> ProsesPembayaran : Validasi Saldo Dompet Virtual
    
    ProsesPembayaran --> SaldoCukup : Saldo Mencukupi
    ProsesPembayaran --> SaldoKurang : Saldo Tidak Mencukupi
    
    SaldoKurang --> TopUpSaldo : Lakukan Top-Up Saldo Dompet
    TopUpSaldo --> BayarSewa
    
    SaldoCukup --> PotongSaldo : Sistem memotong saldo (Sewa + Jaminan)
    PotongSaldo --> UpdateStatusSewa : Ubah status menjadi "Dibayar"
    UpdateStatusSewa --> TransaksiSukses : Kirim notifikasi transaksi sewa berhasil
    TransaksiSukses --> [*]
```

---

## 2. Alur Pengembalian Barang & Verifikasi (Return & Verification Workflow)

Diagram ini menggambarkan alur ketika Penyewa mengembalikan barang sewaan dan Pemilik Barang (Owner) memverifikasi kondisi barang serta denda yang berlaku.

```mermaid
stateDiagram-v2
    [*] --> MulaiKembalikan : Tenant klik "Kembalikan" di Detail Transaksi
    MulaiKembalikan --> AmbilFotoFisik : Ambil foto kondisi barang saat ini
    AmbilFotoFisik --> BeriUlasan : Input Rating (Bintang 1-5) & Komentar
    BeriUlasan --> SubmitPengembalian : Kirim pengajuan pengembalian ke sistem
    SubmitPengembalian --> MenungguVerifikasi : Status transaksi diubah: "Menunggu Verifikasi"
    
    state VerifikasiOwner {
        [*] --> TerimaBarang : Owner menerima barang fisik
        TerimaBarang --> PeriksaKondisi : Periksa kesesuaian & kerusakan barang
    }
    
    state KeputusanVerifikasi <<choice>>
    PeriksaKondisi --> KeputusanVerifikasi : Apakah barang rusak atau telat kembali?
    
    KeputusanVerifikasi --> KondisiBaik : Tidak (Tepat waktu & kondisi utuh)
    KeputusanVerifikasi --> KondisiBermasalah : Ya (Rusak / Terlambat)
    
    KondisiBaik --> SelesaiNormal : Setujui pengembalian tanpa denda
    SelesaiNormal --> RefundJaminanPenuh : Kembalikan uang jaminan 100% ke saldo Tenant
    
    KondisiBermasalah --> InputDenda : Owner menginput denda kerusakan / keterlambatan
    InputDenda --> SelesaiDenda : Setujui pengembalian dengan denda
    SelesaiDenda --> RefundJaminanPotong : Kembalikan uang jaminan setelah dipotong denda
    
    state ProsesSelesai {
        RefundJaminanPenuh --> SelesaiTransaksi
        RefundJaminanPotong --> SelesaiTransaksi
        SelesaiTransaksi --> [*]
    }
```

---

## 3. Alur Manajemen Katalog oleh Pemilik (CRUD Catalog Workflow)

Diagram ini menggambarkan alur bagaimana Pemilik Barang (Owner) mengelola produk yang disewakan ke dalam katalog aplikasi.

```mermaid
stateDiagram-v2
    [*] --> DashboardOwner : Akses Halaman Dashboard Owner
    DashboardOwner --> KatalogSaya : Buka tab "Katalog Saya"
    
    state AksiKatalog <<choice>>
    KatalogSaya --> AksiKatalog : Pilih Aksi
    
    AksiKatalog --> TambahBarang : Klik "Tambah Barang Baru"
    AksiKatalog --> EditBarang : Klik pada salah satu barang -> "Edit"
    AksiKatalog --> HapusBarang : Klik tombol "Hapus" pada barang
    
    TambahBarang --> InputForm : Isi data (Nama, Kategori, Harga, Jaminan, Denda, Stok, Lokasi, Foto)
    InputForm --> SubmitTambah : Kirim data ke server (POST Multipart)
    SubmitTambah --> RefreshKatalog : Sukses ditambahkan
    
    EditBarang --> AmbilDataLama : Sistem menampilkan data lama barang
    AmbilDataLama --> UbahForm : Ubah kolom data yang ingin diganti
    UbahForm --> SubmitUbah : Kirim data ke server (PUT/POST Multipart)
    SubmitUbah --> RefreshKatalog : Sukses diperbarui
    
    HapusBarang --> KonfirmasiHapus : Tampilkan dialog konfirmasi hapus
    KonfirmasiHapus --> KirimRequestHapus : Kirim delete request ke server
    KirimRequestHapus --> RefreshKatalog : Sukses dihapus
    
    RefreshKatalog --> KatalogSaya
    KatalogSaya --> [*]
```
