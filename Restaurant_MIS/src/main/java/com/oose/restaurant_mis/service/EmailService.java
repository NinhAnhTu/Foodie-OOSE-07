package com.oose.restaurant_mis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${app.email.from}")
    private String fromEmail;
    @Async
    public void sendConfirmationEmail(String toEmail, String customerName, String time, String tableName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ninhanhtu1704@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Xác nhận đặt bàn thành công tại Tastyc");
        message.setText("Chào " + customerName + ",\n\n"
                + "Nhà hàng Tastyc xin thông báo yêu cầu đặt bàn của bạn đã được xác nhận thành công!\n"
                + "- Thời gian: " + time + "\n"
                + "- Bàn được xếp: " + tableName + "\n\n"
                + "Rất hân hạnh được phục vụ bạn!");

        mailSender.send(message);
    }

    @Async
    public void sendRejectionEmail(String toEmail, String customerName, String time, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ninhanhtu1704@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Tastyc - Thông báo hủy đặt bàn");
        message.setText("Chào " + customerName + ",\n\n"
                + "Nhà hàng Tastyc rất tiếc phải thông báo không thể xếp bàn cho bạn vào lúc " + time + ".\n\n"
                + "Lý do: " + reason + "\n\n"
                + "Nếu bạn muốn đổi sang khung giờ khác, vui lòng liên hệ lại với chúng tôi qua email này hoặc hotline của nhà hàng.\n\n"
                + "Trân trọng,\nNhà hàng Tastyc.");

        mailSender.send(message);
    }

    @Async
    public void sendResetEmail(String toEmail, String appUrl, String token) {
        try {
            String resetUrl = appUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Yêu cầu khôi phục mật khẩu - RESTO MIS");
            message.setText("Xin chào,\n\n" +
                    "Bạn đã yêu cầu khôi phục mật khẩu. Vui lòng click vào đường link bên dưới để đặt lại mật khẩu mới:\n" +
                    resetUrl + "\n\n" +
                    "Link này sẽ hết hạn sau 15 phút.\n" +
                    "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.");

            mailSender.send(message);
        } catch (Exception e) {
            // Log lỗi ra console để dev biết, người dùng không bị ảnh hưởng
            System.err.println("Lỗi gửi email ngầm: " + e.getMessage());
        }
    }
}