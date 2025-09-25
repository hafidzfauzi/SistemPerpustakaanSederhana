package service;

import org.example.model.Peminjaman;
import org.example.model.Anggota;
import org.example.model.Anggota.TipeAnggota;
import org.example.service.KalkulatorDenda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Kalkulator Denda")
class KalkulatorDendaTest {

    private KalkulatorDenda kalkulatorDenda;
    private Anggota anggotaMahasiswa;
    private Anggota anggotaDosen;
    private Anggota anggotaUmum;

    @BeforeEach
    void setUp() {
        kalkulatorDenda = new KalkulatorDenda();
        anggotaMahasiswa = new Anggota("M001", "John Student", "john@student.ac.id",
                "081234567890", Anggota.TipeAnggota.MAHASISWA);
        anggotaDosen = new Anggota("D001", "Dr. Jane", "jane@dosen.ac.id",
                "081234567891", Anggota.TipeAnggota.DOSEN);
        anggotaUmum = new Anggota("U001", "Public User", "public@email.net",
                "081234567892", Anggota.TipeAnggota.UMUM);
    }

    @Test
    @DisplayName("Tidak ada denda untuk peminjaman yang tidak terlambat")
    void testTidakAdaDendaUntukPeminjamanTidakTerlambat() {
        LocalDate tanggalPinjam = LocalDate.now().minusDays(3);
        LocalDate tanggalJatuhTempo = LocalDate.now().plusDays(3);

        Peminjaman peminjaman = new Peminjaman("P001", "M001", "1234567890", tanggalPinjam, tanggalJatuhTempo);

        double denda = kalkulatorDenda.hitungDenda(peminjaman, anggotaMahasiswa);

        assertEquals(0.0, denda, "Denda harus 0 untuk peminjaman yang tidak terlambat");
    }

    @Test
    @DisplayName("Hitung denda mahasiswa 3 hari terlambat")
    void testHitungDendaMahasiswa3HariTerlambat() {
        LocalDate tanggalPinjam = LocalDate.now().minusDays(10);
        LocalDate tanggalJatuhTempo = LocalDate.now().minusDays(3); // 3 hari terlambat

        Peminjaman peminjaman = new Peminjaman("P001", "M001", "1234567890", tanggalPinjam, tanggalJatuhTempo);

        double dendaAktual = kalkulatorDenda.hitungDenda(peminjaman, anggotaMahasiswa);

        assertEquals(3000.0, dendaAktual, "3 hari * 1000 harus sama dengan 3000");
    }

    @Test
    @DisplayName("Hitung denda dosen 5 hari terlambat")
    void testHitungDendaDosen() {
        LocalDate tanggalPinjam = LocalDate.now().minusDays(10);
        LocalDate tanggalJatuhTempo = LocalDate.now().minusDays(5); // 5 hari terlambat

        Peminjaman peminjaman = new Peminjaman("P001", "D001", "1234567890", tanggalPinjam, tanggalJatuhTempo);

        double dendaAktual = kalkulatorDenda.hitungDenda(peminjaman, anggotaDosen);

        assertEquals(10000.0, dendaAktual, "5 hari * 2000 harus sama dengan 10000");
    }

    @Test
    @DisplayName("Denda tidak boleh melebihi batas maksimal")
    void testDendaTidakMelebihiBatasMaksimal() {
        // Peminjaman sangat terlambat (100 hari)
        LocalDate tanggalPinjam = LocalDate.now().minusDays(107);
        LocalDate tanggalJatuhTempo = LocalDate.now().minusDays(100);

        Peminjaman peminjaman = new Peminjaman("P001", "M001", "1234567890", tanggalPinjam, tanggalJatuhTempo);

        double dendaAktual = kalkulatorDenda.hitungDenda(peminjaman, anggotaMahasiswa);

        assertEquals(50000.0, dendaAktual, "Denda tidak boleh melebihi batas maksimal mahasiswa");
    }

    @Test
    @DisplayName("Exception untuk parameter null")
    void testExceptionParameterNull() {
        assertThrows(IllegalArgumentException.class, () ->
                        kalkulatorDenda.hitungDenda(null, anggotaMahasiswa),
                "Harus throw exception untuk peminjaman null");

        Peminjaman peminjaman = new Peminjaman("P001", "M001", "1234567890", LocalDate.now(), LocalDate.now().plusDays(3));

        assertThrows(IllegalArgumentException.class, () ->
                        kalkulatorDenda.hitungDenda(peminjaman, null),
                "Harus throw exception untuk anggota null");
    }

    @Test
    @DisplayName("Cek tarif denda harian sesuai tipe anggota")
    void testGetTarifDendaHarian() {
        assertEquals(1000.0, kalkulatorDenda.getTarifDendaHarian(TipeAnggota.MAHASISWA));
        assertEquals(2000.0, kalkulatorDenda.getTarifDendaHarian(TipeAnggota.DOSEN));
        assertEquals(1500.0, kalkulatorDenda.getTarifDendaHarian(TipeAnggota.UMUM));

        assertThrows(IllegalArgumentException.class, () -> kalkulatorDenda.getTarifDendaHarian(null));
    }

    @Test
    @DisplayName("Cek denda maksimal sesuai tipe anggota")
    void testGetDendaMaximal() {
        assertEquals(50000.0, kalkulatorDenda.getDendaMaximal(TipeAnggota.MAHASISWA));
        assertEquals(100000.0, kalkulatorDenda.getDendaMaximal(TipeAnggota.DOSEN));
        assertEquals(75000.0, kalkulatorDenda.getDendaMaximal(TipeAnggota.UMUM));

        assertThrows(IllegalArgumentException.class, () -> kalkulatorDenda.getDendaMaximal(null));
    }

    @Test
    @DisplayName("Cek ada denda")
    void testAdaDenda() {
        // Peminjaman terlambat
        LocalDate tanggalJatuhTempoTerlambat = LocalDate.now().minusDays(1);
        Peminjaman peminjamanTerlambat = new Peminjaman("P001", "M001", "1234567890", LocalDate.now().minusDays(5), tanggalJatuhTempoTerlambat);

        assertTrue(KalkulatorDenda.adaDenda(peminjamanTerlambat));

        // Peminjaman tidak terlambat
        Peminjaman peminjamanTidakTerlambat = new Peminjaman("P002", "M001", "1234567890", LocalDate.now().minusDays(3), LocalDate.now().plusDays(1));

        assertFalse(KalkulatorDenda.adaDenda(peminjamanTidakTerlambat));

        // Peminjaman null
        assertFalse(KalkulatorDenda.adaDenda(null));
    }

    @Test
    @DisplayName("Deskripsi denda sesuai jumlah")
    void testDeskripsiDenda() {
        assertEquals("Tidak ada denda", KalkulatorDenda.getDeskripsiDenda(0.0));
        assertEquals("Denda ringan", KalkulatorDenda.getDeskripsiDenda(5000.0));
        assertEquals("Denda sedang", KalkulatorDenda.getDeskripsiDenda(25000.0));
        assertEquals("Denda berat", KalkulatorDenda.getDeskripsiDenda(75000.0));
        //
    }
}