package com.restphone.jartender

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith( classOf[ JUnitRunner ] )
class JavaSignatureParserTest extends FunSuite {
  val p = new JavaSignatureParser

  test( "can parse a Java primitive" ) {
    expectResult( "boolean" ) {
      p.parseAll( p.typeSignature, "Z" ).get.toJava
    }
  }

  test( "can parse java.util.List<E>" ) {
    expectResult( "java.util.List<E>" ) {
      p.parseAll( p.typeSignature, "Ljava/util/List<TE;>;" ).get.toJava
    }
  }

  test( "can parse Number" ) {
    expectResult( "Number" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/lang/Number;" )
      result.get.toJava
    }
  }

  test( "can parse List<Number>" ) {
    expectResult( "java.util.List<Number>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/List<Ljava/lang/Number;>;" )
      result.get.toJava
    }
  }

  test( "can parse java.util.List<? extends Number>" ) {
    expectResult( "java.util.List<? extends Number>" ) {
      val s = "Ljava/util/List<+Ljava/lang/Number;>;"
      JavaSignatureParser.parse( s ).get.toJava
    }
  }

  test( "can parse java.util.List<? super Number>" ) {
    expectResult( "java.util.List<? super Number>" ) {
      val s = "Ljava/util/List<-Ljava/lang/Number;>;"
      JavaSignatureParser.parse( s ).get.toJava
    }
  }

  test( "can parse List<List<String>[]>" ) {
    expectResult( "java.util.List<java.util.List<String>[]>" ) {
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
