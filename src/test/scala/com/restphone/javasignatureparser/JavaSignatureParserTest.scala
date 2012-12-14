package com.restphone.javasignatureparser

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class JavaSignatureParserTest extends FunSuite with ShouldMatchers{
  val p = new JavaSignatureParser

  test( "can parse a Java primitive" ) {
    expectResult( "boolean" ) {
      p.parseAll( p.typeSignature, "Z" ).get.toJava
    }
  }

  test( "can parse a Typevar" ) {
    expectResult( "foo.bar$" ) {
      p.parseAll( p.typeVar, "Tfoo/bar$;" ).get.toJava
    }
  }

  test( "can parse java.util.List<E>" ) {
    expectResult( "java.util.List<E>" ) {
      p.parseAll( p.typeSignature, "Ljava/util/List<TE;>;" ).get.toJava
    }
  }

  test( "can parse Number" ) {
    expectResult( "java.lang.Number" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/lang/Number;" )
      result.get.toJava
    }
  }

  test( "can parse List<Number>" ) {
    expectResult( "java.util.List<java.lang.Number>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/List<Ljava/lang/Number;>;" )
      result.get.toJava
    }
  }

  test( "can parse java.util.List<? extends Number>" ) {
    expectResult( "java.util.List<? extends java.lang.Number>" ) {
      val s = "Ljava/util/List<+Ljava/lang/Number;>;"
      JavaSignatureParser.parse( s ).get.toJava
    }
  }

  test( "can parse java.util.List<? super Number>" ) {
    expectResult( "java.util.List<? super java.lang.Number>" ) {
      val s = "Ljava/util/List<-Ljava/lang/Number;>;"
      JavaSignatureParser.parse( s ).get.toJava
    }
  }

  test( "can parse List<List<String>[]>" ) {
    expectResult( "java.util.List<java.util.List<java.lang.String>[]>" ) {
      val s = "Ljava/util/List<[Ljava/util/List<Ljava/lang/String;>;>;"
      JavaSignatureParser.parse( s ).get.toJava
    }
  }

  test( "can parse HashMap<K, V>.HashIterator<K>" ) {
    expectResult( "java.util.HashMap<K, V>.HashIterator<K, V>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/HashMap<TK;TV;>.HashIterator<TK;TV;>;" )
      result.get.toJava
    }
  }

  test( "can parse java.util.List<?>" ) {
    expectResult( "java.util.List<?>" ) {
      JavaSignatureParser.parse( "Ljava/util/List<*>;" ).get.toJava
    }
  }
  
  test("can parse Lorg/scalatest/matchers/ClassicMatchers$ShortTolerance$;") {
    val s = "Lorg/scalatest/matchers/ClassicMatchers$ShortTolerance$;"
    val r = JavaSignatureParser.parse(s)
    r.get.toJava should be ("org.scalatest.matchers.ClassicMatchers$ShortTolerance$")
  }

  test( "can parse (II)V" ) {
    expectResult( "void methodName ( int , int )" ) {
      JavaSignatureParser.parseMethod( "(II)V" ).get.toJava
    }
  }
  
  test( "can get the right set of java types that are used in java.util.List<? extends Number>" ) {
    expectResult( Set(JavaName("java.util.List"), JavaName("java.lang.Number")) ) {
      val s = "Ljava/util/List<+Ljava/lang/Number;>;"
      JavaSignatureParser.parse( s ).get.typesUsed
    }
  }
}
