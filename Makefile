.PHONY: run build clean

build:
	javac -d bin $(shell find src -name "*.java")

run: build
	java -cp bin Main

clean:
	rm -rf bin
