# Makefile for JIFDY (Java Information Flow Dolev-Yao) Project

# What does the makefile do? It will do the following processes:
# 1. To build the project: 'make' or 'make compile'
# 2. To run the regression tests: 'make test'
# 3. To run the compiler: 'make run'
# 4. To clean the project: 'make clean'

# Variables
MVN = mvn
# Default Java path. Set JAVA_HOME if you want to use a specific JDK.
JAVA = java

# Default target: compile everything
all: compile

# Phase 1: Compilation
# This phase uses Maven to compile all Java sources and ANTLR generated files.
# Requires JDK 17+
compile:
	@echo "Compiling the project..."
	$(MVN) compile

# Phase 2: Testing
# This phase runs the RegressionTest suite which checks all information flow examples.
# It verifies that information flow violations are caught and reported.
test: compile
	@echo "Running regression tests..."
	$(MVN) exec:java -Dexec.mainClass="Analysis.RegressionTest"

# Phase 3: Running a specific file
# This phase runs the main Compiler on the default BankTransfer example.
run: compile
	@echo "Running the compiler on BankTransfer.jifdy..."
	$(MVN) exec:java -Dexec.mainClass="Compiler"

# Phase 4: Cleanup
# Removes all compiled classes and generated artifacts.
clean:
	@echo "Cleaning up..."
	$(MVN) clean
