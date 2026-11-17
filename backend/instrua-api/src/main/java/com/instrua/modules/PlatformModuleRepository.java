package com.instrua.modules;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformModuleRepository extends JpaRepository<PlatformModule, String> {
    List<PlatformModule> findAllByActiveTrueOrderByDisplayOrderAsc();
}
