# EPlan - Automated ETCS Planning Tool

EPlan is a command line tool that allows the fully automated planning of railway lines equipped with the European Train Control System (ETCS) for the German Railway Network.
It supports both ETCS Level 1 (according to the rules of the DB Ril 819.1348) and ETCS Level 2 (Ril 819.1344).

To use EPlan, the planning of the underlying interlocking system has to be finished (which is also required by Ril 819.1344), and all objects that are used as reference points (like signals) must already have the correct position.
EPlan uses this plan as input, and adds aditional ETCS components to it.

The input plan must be a PPXML file in the [PlanPro](http://www.dbinfrago.com/planpro) format, and the generated output will have the same format.

## Download

We do not provide any executable binaries.
Please build EPlan from source using the instructions below.


## Building from Source

To build EPlan, you need a Java JDK (Java 17 or higher). All other dependencies will be resolved by the included Gradle wrapper.

Open a command line, navigate to the project root directory and enter `./gradlew distZip` on Linux/macOS or `gradlew distZip` on Windows.
After a successful build, you can find in the directory `build/distributions` a file named `eplan-x.y.z-SNAPSHOT.zip` (where `x.y.z` is the version number).
This archive can now be distributed to different target machines. All these computers need to have a Java Runtime (Java 17 or higher) installed.

Extract the ZIP archive at the designated target location. Open a command line, navigate to the `bin` directory and enter `./eplan --help` (on Windows `eplan --help`)..
If you can see the usage instructions, everything works.

It is recommended to include the `bin` directory to your PATH environment variable (or create a symlink from one of your system's `bin` directories to the `eplan` start script).


## Usage

The general call syntax is `eplan [options] inputfile`. The possible options can be shown with `eplan --help`. The most commonly used options are

- `-o outputfile` to specify the file where the final plan should be written to (without this option, the plan is written to standard output)
- `-l level` to specify the ETCS Level (allowed values are `-l1` for ETCS Level 1 and `-l2` for ETCS Level 2,
  without this option, Level 2 is used as default))
- `-c` to activate the compatibility mode for old PlanPro files (converted form older PlanPro versions), where the fouling point indicator and point lantern are transposed
  (the included P-Hausen example station needs this option)
- `--stdin` to read the input plan from standard input

A typical command looks like the following:

```
eplan -l2 -o outputfile.ppxml inputfile.ppxml
```

Instead of inputfile.ppxml and outputfile.ppxml use your real file names (possibly including the full paths).

## Limitations

Due to its origin as a research project, EPlan has incomplete and simplified functionality, and should be seen rather as a proof of concept than as fully featured planning tool.
Especially the following limitations have to be taken into account:

- EPlan supports input files only in PlanPro version 1.9.0, and only the plain XML files (file extension *.ppxml) can be processed.
  Plans in the zipped container format (file extension *.planpro) are not supported.
- The input plan is expected to be valid against the PlanPro 1.9.0 XML schema, but the program does not check this.
  A malformed input file (where required elements are missing) may lead to a program crash.
- It is not ensured that the created plans can be read error-free with tools like the PlanPro toolbox or ProSig.
  It is however possible to read them with [PlanPro Viewer](https://github.com/formetcs/ppview).
- The input file needs a planning end state (PlanPro attribute `LST_Zustand_Ziel`), and all objects used as input for the ETCS planning (track geometry, signals) must be available in this state.
  If no end state is existing in the input file, a new end state can be created (as copy of the start state) with the `--create-end` option.
- Only Datapoint and Balise objects will be added to the final plan.
- For ETCS Level 1, rules for the following datapoint types are implemented (according to Ril 819.1348): HS, MS, VS, VW, AW.
- For ETCS Level 2, rules for the following datapoint types are implemented (according to Ril 819.1344): 9, 20, 21, 22, 23, 24, 25 (gap fill use case only), 26, 28.
- The main focus of the rule implementation was the positioning of the objects. The setting of all other object attributes may be incorrect.


## Licensing

The EPlan source code, and the resulting binaries (the EPlan JAR), are licensed under the terms of the [Modified (3-Clause) BSD License](LICENSE).

The P-Hausen example has been taken from the [Signalling Engineering Toolbox](https://projects.eclipse.org/projects/technology.set), licensed under the Eclipse Public License V2.0. Used with permission of DB InfraGO AG.

This software uses the PlanPro Data Model, licensed under the RailPL Open Source License. Visit http://www.dbinfrago.com/planpro to get more information.

A binary distribution of EPlan may contain additional third-party libraries. Please see their respective license files for details.


## References

Stefan Dillmann and Reiner Hähnle. “Automated Planning of ETCS Tracks”.
In: Reliability, Safety and Security of Railway Systems: Modelling, Analysis, Verification and Certification (RSSRail), 2019, Lille, France.
LNCS, Vol. 11495, Springer, pp. 79–90.
[https://doi.org/10.1007/978-3-030-18744-6_5](https://doi.org/10.1007/978-3-030-18744-6_5)
