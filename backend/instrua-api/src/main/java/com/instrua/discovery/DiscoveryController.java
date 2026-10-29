package com.instrua.discovery;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discovery")
public class DiscoveryController {
    private final JdbcTemplate jdbc;
    public DiscoveryController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/companies")
    public List<Map<String,Object>> companies(@RequestParam(defaultValue="") String query, @RequestParam(required=false) String niche){
        String q="%"+query.trim().toLowerCase()+"%";
        String n=niche==null?null:niche.trim().toLowerCase();
        return jdbc.query("""
            select id,name,niche,address_line as "addressLine",latitude,longitude
            from companies
            where active=true and (lower(name) like ? or lower(niche) like ?)
              and (? is null or lower(niche)=?)
            order by name asc
            limit 50
            """,(rs,n)->Map.of("id",rs.getObject("id"),"name",rs.getString("name"),
                "niche",rs.getString("niche"),"addressLine",rs.getString("addressLine")==null?"":rs.getString("addressLine"),
                "latitude",rs.getObject("latitude")==null?0:rs.getObject("latitude"),
                "longitude",rs.getObject("longitude")==null?0:rs.getObject("longitude")),q,q,n,n);
    }
}
