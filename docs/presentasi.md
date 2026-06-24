# OUTLINE SLIDE PRESENTASI & PEMBAGIAN MATERI: **SEWAIN**

Dokumen ini disesuaikan dengan panduan pembicara untuk **Pembicara 1**, **Pembicara 2**, dan **Pembicara 3** agar alur presentasi berjalan lancar dan profesional.

---

## Slide 1: Halaman Judul
*   **Judul**: Sewain - Platform Sewa Barang Modern berbasis Android
*   **Subjudul**: Menghubungkan Tenant & Owner dengan Aman dan Mudah
*   **Tim Presenter**: Kelompok 10 - Praktikum PAB
*   **Pembicara**: [Nama Pembicara 1, 2, dan 3]

---

## Slide 2: Latar Belakang & Masalah (Pembicara 1)
*   **Poin Utama**:
    *   **Barang Menganggur**: Peralatan hobi/camping/kamera sering kali hanya disimpan dan jarang terpakai.
    *   **Akses Terbatas**: Masyarakat kesulitan menyewa barang tepercaya secara instan di area terdekat.
    *   **Keamanan Transaksi**: Kekhawatiran kehilangan barang oleh pemilik, serta dana jaminan yang tidak transparan bagi penyewa.

---

## Slide 3: Solusi & Tujuan Aplikasi (Pembicara 1)
*   **Solusi (Sewain)**: Menyediakan jembatan digital multi-owner & multi-tenant untuk penyewaan barang.
*   **Tujuan**:
    *   Membantu pemilik barang (*Owner*) mendapatkan penghasilan tambahan.
    *   Memudahkan penyewa (*Tenant*) mendapatkan barang dengan harga sewa terjangkau + dana jaminan transparan.

---

## Slide 4: Gambaran Umum Fitur (Pembicara 1)
*   **Fitur Tenant**: Registrasi, Pencarian & Filter Kategori, Keranjang Estimasi Hari, Checkout & Pembayaran E-Wallet.
*   **Fitur Owner**: Dashboard Statistik Saldo, CRUD Katalog Barang (Upload Foto), dan Verifikasi Pengembalian + Input Denda.

---

## Slide 5: Arsitektur & Teknologi (Pembicara 2 - Durasi: ± 1 Menit)
*   **Bahasa Utama**: Kotlin
*   **UI Toolkit**: Jetpack Compose (Modern, deklaratif, Material Design 3)
*   **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern (Pemisahan logika & data stream)
*   **Networking**: Retrofit 2 & OkHttp (Intersepsi log REST API backend)
*   **Media & Sesi**: Coil Compose (Pemuatan gambar dinamis) & SharedPreferences (`SessionManager` untuk token JWT)

---

## Slide 6: Judul Transisi — LIVE DEMO APLIKASI (Pembicara 2 - Durasi: ± 7-8 Menit)
*   **Fokus Live Demo**:
    *   **Sesi Awal**: Demo Login/Register secara singkat (gunakan akun dummy yang sudah siap).
    *   **Alur Tenant (Penyewa)**:
        1. Buka halaman Katalog Publik -> Lakukan pencarian & filter kategori.
        2. Masuk ke Detail Produk untuk melihat tarif sewa, jaminan, dan denda.
        3. Tambah barang ke Keranjang -> Atur tanggal pinjam & kembali.
        4. Lakukan Checkout dan selesaikan Pembayaran menggunakan saldo dompet virtual.
    *   **Alur Owner (Pemilik)**:
        1. Buka Dashboard Owner untuk melihat statistik total barang dan saldo pendapatan.
        2. Masuk ke Katalog Saya -> Tambahkan barang baru / edit stok barang.
    *   **Alur Akhir (Opsional - Jika waktu cukup)**:
        1. Tenant melakukan pengajuan pengembalian barang + unggah bukti foto + rating.
        2. Owner memeriksa kiriman pengembalian tersebut dan memverifikasinya.

---

## Slide 7: Kendala Teknis (Pembicara 3 - Durasi: ± 45 Detik)
*   *Fokus pada 2 kendala teknis paling signifikan:*
    1.  **Sinkronisasi Token Auth**: Penanganan sesi kedaluwarsa token JWT Bearer pada `SharedPreferences` agar user otomatis logout secara aman saat token backend mati.
    2.  **Pemrosesan Upload Multipart**: Mengelola pengiriman data form dan file media (foto profil, foto barang, bukti pengembalian) secara asinkron menggunakan Coroutines agar tidak memblokir UI thread.

---

## Slide 8: Pencapaian & Kesimpulan (Pembicara 3 - Durasi: ± 1,5 Menit)
*   *3 Insight Utama:*
    1.  **Pola MVVM Terstruktur**: Pemisahan logika UI, data lokal, dan API mempermudah pemeliharaan dan skalabilitas aplikasi.
    2.  **UI Reaktif dengan Jetpack Compose**: Membuat performa rendering antarmuka jauh lebih lancar, responsif, dan hemat baris kode.
    3.  **Integrasi End-to-End**: Berhasil mengintegrasikan aplikasi Android dengan REST API backend Laravel secara real-time untuk siklus penyewaan penuh.

---

## Slide 9: Rencana Masa Depan (Pembicara 3 - Durasi: ± 45 Detik)
*   **Skalabilitas & Pengembangan**:
    1.  **Payment Gateway Integration**: Integrasi gerbang pembayaran otomatis pihak ketiga (seperti Midtrans) untuk top-up dan pembayaran sewa langsung.
    2.  **Google Maps API**: Menampilkan lokasi real-time barang sewaan terdekat langsung pada peta interaktif.
    3.  **Real-time Notification**: Menggunakan Firebase Cloud Messaging (FCM) untuk notifikasi instan status sewa dan peringatan keterlambatan.

---

## Slide 10: Penutup & Tanya Jawab (Pembicara 3 - Durasi: ± 15 Detik)
*   **Penutup**: Terima Kasih atas perhatiannya.
*   **Sesi**: Membuka sesi diskusi dan Tanya Jawab (Q&A) dengan audiens.
