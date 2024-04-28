package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;

public class Server extends Node {
    String code;

    public Server(int line, int linePosition, String name, String code) {
        super(line, linePosition, name);
        this.code = code;
    }

    @Override
    public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
        return null;
    }

    @Override
    public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
        if (this.code == null) {
            return;
        }

        ThunderAnalyzer thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.TYPE_CHECKER);
        thunderAnalyzer.analyze(this.code);
        compilerErrors.addAll(thunderAnalyzer.getErrors());
    }
}
