package com.instrua.instructions;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructionRepository extends JpaRepository<Instruction, UUID> {
    List<Instruction> findAllByCompanyIdOrderByDisplayOrderAsc(UUID companyId);
    List<Instruction> findAllByCompanyIdAndServiceOfferingIdOrderByDisplayOrderAsc(UUID companyId, UUID serviceOfferingId);
}
