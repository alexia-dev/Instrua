package com.instrua.ai;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiToolService {
    private final JdbcTemplate jdbc;
    public AiToolService(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public Map<String,Object> listBookings(UUID userId){
        List<Map<String,Object>> rows=jdbc.queryForList("""
            select id, starts_at as "startsAt", status from appointments
            where customer_id=? order by starts_at asc limit 20
            """,userId);
        return Map.of("bookings",rows);
    }

    public Map<String,Object> checkAvailability(UUID userId){
        List<Map<String,Object>> rows=jdbc.queryForList("""
            select id, starts_at as "startsAt", ends_at as "endsAt", status
            from appointments where customer_id=? and starts_at>=now()
            order by starts_at asc limit 10
            """,userId);
        return Map.of("upcoming",rows);
    }

    public Map<String,Object> getInstructions(UUID userId){
        List<Map<String,Object>> rows=jdbc.queryForList("""
            select i.id,i.title,i.body,i.phase from instructions i
            join appointments a on a.service_offering_id=i.service_offering_id
            where a.customer_id=? order by a.starts_at asc limit 20
            """,userId);
        return Map.of("instructions",rows);
    }
}
