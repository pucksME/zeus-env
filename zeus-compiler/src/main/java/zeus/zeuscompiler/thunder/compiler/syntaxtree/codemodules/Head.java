package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Head {
  Map<String, Input> inputs;
  Map<String, Output> outputs;
  Map<String, Config> configs;

  public Head(List<Input> inputs, List<Output> outputs, List<Config> configs) {
    // https://stackoverflow.com/a/20887747 [accessed 20/4/2023, 12:26]
    this.inputs = inputs.stream().collect(Collectors.toMap(Input::getId, input -> input));
    this.outputs = outputs.stream().collect(Collectors.toMap(Output::getId, output -> output));
    this.configs = configs.stream().collect(Collectors.toMap(Config::getId, config -> config));
  }
}
