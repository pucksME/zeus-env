package zeus.zeuscompiler.thunder.dtos;

import zeus.zeuscompiler.rain.dtos.LocationDto;
import zeus.zeuscompiler.rain.dtos.VariableAssignmentDto;

import java.util.List;
import java.util.Set;

public record CodeModuleCounterexampleDto(List<LocationDto> locations, Set<VariableAssignmentDto> variableAssignments) {
}
