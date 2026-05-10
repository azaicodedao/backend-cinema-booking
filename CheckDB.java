import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/cinema_test", "root", "khoakhoa_2910");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT t.id, t.seat_id, s.room_id, b.id as booking_id, b.status FROM tickets t JOIN bookings b ON t.booking_id = b.id JOIN seats s ON t.seat_id = s.id WHERE b.showtime_id = 1 AND b.status = 'CONFIRMED'");
            while (rs.next()) {
                System.out.println("Ticket: " + rs.getInt("id") + ", Seat: " + rs.getInt("seat_id") + ", Room: " + rs.getInt("room_id") + ", Booking: " + rs.getInt("booking_id"));
            }
            
            System.out.println("Now querying showtime room id...");
            rs = stmt.executeQuery("SELECT room_id FROM showtimes WHERE id = 1");
            if (rs.next()) {
                System.out.println("Showtime 1 Room ID: " + rs.getInt("room_id"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
