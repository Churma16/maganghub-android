package id.codes.maganghub.data.model

/**
 * Representation of daily attendance status on MagangHub.
 */
enum class AttendanceStatus(val label: String, val isAttended: Boolean) {
    HADIR("Hadir", true),
    BELUM_HADIR("Belum Hadir", false),
    LIBUR("Hari Libur", true),
    TIDAK_AKTIF("Magang Tidak Aktif", true),
    UNKNOWN("Status Tidak Diketahui", false);

    companion object {
        fun fromHomeData(data: AttendanceHomeData?): AttendanceStatus {
            if (data == null) return UNKNOWN
            if (!data.activeInternship) return TIDAK_AKTIF
            if (data.isHoliday || data.isScheduledOffDay) return LIBUR
            if (data.hasAttendance) return HADIR
            return BELUM_HADIR
        }
    }
}
