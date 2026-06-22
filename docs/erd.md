# ENTITY RELATIONSHIP DIAGRAM (ERD): **SEWAIN**

Dokumen ini memuat Entity Relationship Diagram (ERD) database relasional dari sistem aplikasi backend **Sewain**, yang dirancang untuk mendukung operasional multi-owner dan multi-tenant.

---

## 1. Diagram ERD 

```mermaid
erDiagram
    USERS ||--o{ BARANG : "memiliki (owner)"
    USERS ||--o{ TRANSAKSI : "menyewa (tenant)"
    USERS ||--o{ KERANJANG_ITEMS : "memiliki"
    USERS ||--o{ REVIEWS : "menulis"
    
    KATEGORI ||--o{ BARANG : "mengelompokkan"
    
    BARANG ||--o{ KERANJANG_ITEMS : "dimasukkan"
    BARANG ||--o{ TRANSAKSI : "disewa"
    BARANG ||--o{ REVIEWS : "menerima"

    TRANSAKSI ||--o| PEMBAYARAN : "dibayar dengan"
    TRANSAKSI ||--o| REVIEWS : "memiliki"

    USERS {
        int id PK
        string name "Nama Lengkap"
        string username "Username unik"
        string email "Email (Unique, Kunci login)"
        string phone_number "No HP"
        string alamat "Alamat kirim"
        double saldo "Saldo Dompet Virtual"
        string foto_profil "Nama file foto profil"
        string role "Role (tenant/owner)"
        boolean is_banned "Status pemblokiran"
        date tanggal_lahir "Tanggal lahir"
        string jenis_kelamin "Jenis kelamin (L/P)"
    }

    KATEGORI {
        int id PK
        string nama_kategori "Nama Kategori (eg. Kamera, Camping)"
        string deskripsi "Deskripsi singkat"
    }

    BARANG {
        int id PK
        int user_id FK "Relasi ke USERS (Pemilik)"
        int kategori_id FK "Relasi ke KATEGORI"
        string nama_barang "Nama produk sewa"
        string deskripsi "Deskripsi produk"
        string additional_information "Informasi tambahan"
        double harga_sewa "Tarif sewa per hari"
        double harga_jaminan "Dana jaminan wajib"
        double harga_denda_perjam "Denda keterlambatan per jam"
        int stok "Stok barang tersedia"
        string lokasi "Lokasi barang berada"
        string foto_barang "Nama file foto produk"
        string status "Status barang (Tersedia/Disewa/Nonaktif)"
    }

    KERANJANG_ITEMS {
        int id PK
        int user_id FK "Relasi ke USERS (Penyewa)"
        int barang_id FK "Relasi ke BARANG"
        int jumlah "Kuantitas barang yang dipesan"
        date tanggal_sewa "Tanggal mulai rencana sewa"
        date tanggal_kembali_rencana "Tanggal selesai rencana sewa"
        double subtotal "Kalkulasi biaya sewa sementara"
    }

    PEMBAYARAN {
        int id PK
        string metode "Metode (e-wallet/bank)"
        string detail_metode "Rincian akun/rekening"
        date tanggal_bayar "Waktu penyelesaian pembayaran"
        double jumlah_bayar "Total dana yang didebit"
    }

    TRANSAKSI {
        int id PK
        int user_id FK "Relasi ke USERS (Penyewa)"
        int barang_id FK "Relasi ke BARANG"
        int pembayaran_id FK "Relasi ke PEMBAYARAN (Nullable)"
        int jumlah "Kuantitas barang"
        date tanggal_sewa "Tanggal mulai sewa"
        date tanggal_kembali_rencana "Tanggal rencana kembali"
        date tanggal_kembali_aktual "Tanggal aktual barang kembali (Nullable)"
        string status "Status sewa (Menunggu Pembayaran/Disewa/Kembali/Selesai/Denda)"
        string foto_buktipengembalian "Bukti foto pengembalian (Nullable)"
        date tanggal_verifikasipengembalian "Tanggal verifikasi owner"
        double total_harga "Total tarif sewa"
        int jam_terlambat "Jumlah durasi terlambat dalam jam"
        double total_denda "Total tagihan denda keterlambatan/kerusakan"
    }

    REVIEWS {
        int id PK
        int transaksi_id FK "Relasi ke TRANSAKSI (Satu transaksi = Satu review)"
        int user_id FK "Relasi ke USERS (Penulis)"
        int barang_id FK "Relasi ke BARANG"
        int rating "Nilai Bintang (1 s.d 5)"
        string komentar "Ulasan tertulis"
        string foto_review "Bukti ulasan visual"
    }
```

---

## 2. Kardinalitas & Aturan Bisnis Relasi Database

1.  **USERS ke BARANG (1 to Many)**:
    *   Satu user (sebagai Owner) dapat memiliki dan mendaftarkan **banyak** barang di katalog.
    *   Satu barang hanya didaftarkan oleh **satu** user.
2.  **KATEGORI ke BARANG (1 to Many)**:
    *   Satu kategori (misal: "Kamera") dapat mencakup **banyak** barang.
    *   Satu barang hanya terikat pada **satu** kategori utama.
3.  **USERS ke TRANSAKSI (1 to Many)**:
    *   Satu user (sebagai Tenant/Penyewa) dapat melakukan **banyak** transaksi sewa dari waktu ke waktu.
    *   Satu transaksi hanya dibuat oleh **satu** user penyewa.
4.  **BARANG ke TRANSAKSI (1 to Many)**:
    *   Satu barang dapat disewa dalam **banyak** transaksi (berbeda waktu/penyewa).
    *   Satu baris transaksi hanya mengaitkan **satu** spesifik barang.
5.  **TRANSAKSI ke PEMBAYARAN (Many to 1 atau 1 to 1)**:
    *   Setiap transaksi sewa yang terbayar terikat dengan **satu** catatan pembayaran. Kolom `pembayaran_id` bersifat nullable karena transaksi baru belum dibayar.
6.  **TRANSAKSI ke REVIEWS (1 to 1)**:
    *   Setiap transaksi penyewaan yang sukses diselesaikan hanya dapat menghasilkan **satu** ulasan/review barang untuk mencegah spam ulasan ganda.
