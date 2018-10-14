[![Build Status](https://travis-ci.org/Warchant/testcontainers-iroha.svg?branch=master)](https://travis-ci.org/Warchant/testcontainers-iroha)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ce56f4b975e1469da6b7ecfc8b98d879)](https://www.codacy.com/app/Warchant/testcontainers-iroha?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Warchant/testcontainers-iroha&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/Warchant/testcontainers-iroha/branch/master/graph/badge.svg)](https://codecov.io/gh/Warchant/testcontainers-iroha)
[![](https://jitpack.io/v/Warchant/testcontainers-iroha.svg)](https://jitpack.io/#Warchant/testcontainers-iroha)

# testcontainers-iroha
Testcontainers image for single Iroha peer


# Install

https://jitpack.io/#warchant/testcontainers-iroha

# Usage

```java

class MyTest {
  
  @Rule
  IrohaContainer iroha = new IrohaContainer();
  
  @Test
  public TestWithIroha (){
    String toriiAddr = iroha.getToriiAddress();  // iroha API host:port (torii)
    // ...
  }
}

```
