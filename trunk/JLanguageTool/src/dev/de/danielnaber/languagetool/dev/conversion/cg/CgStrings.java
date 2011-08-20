package de.danielnaber.languagetool.dev.conversion.cg;

public class CgStrings {

    public enum KEYWORDS {
        // list of types of keywords to parse in parseRule, e.g. ADD, REPLACE
        K_IGNORE (0),
        K_SETS (1),
        K_LIST (2),
        K_SET (3),
        K_DELIMITERS (4),
        K_SOFT_DELIMITERS (5),
        K_PREFERRED_TARGETS (6),
        K_MAPPING_PREFIX (7),
        K_MAPPINGS (8),
        K_CONSTRAINTS (9),
        K_CORRECTIONS (10),
        K_SECTION (11),
        K_BEFORE_SECTIONS (12),
        K_AFTER_SECTIONS (13),
        K_NULL_SECTION (14),
        K_ADD (15),
        K_MAP (16),
        K_REPLACE (17),
        K_SELECT (18),
        K_REMOVE (19),
        K_IFF (20),
        K_APPEND (21),
        K_SUBSTITUTE (22),
        K_START (23),
        K_END (24),
        K_ANCHOR (25),
        K_EXECUTE (26),
        K_JUMP (27),
        K_REMVARIABLE (28),
        K_SETVARIABLE (29),
        K_DELIMIT (30),
        K_MATCH (31),
        K_SETPARENT (32),
        K_SETCHILD (33),
        K_ADDRELATION (34),
        K_SETRELATION (35),
        K_REMRELATION (36),
        K_ADDRELATIONS (37),
        K_SETRELATIONS (38),
        K_REMRELATIONS (39),
        K_TEMPLATE (40),
        K_MOVE (41),
        K_MOVE_AFTER (42),
        K_MOVE_BEFORE (43),
        K_SWITCH (44),
        K_REMCOHORT (45),
        K_STATIC_SETS (46),
        K_UNMAP (47),
        K_COPY (48),
        K_ADDCOHORT (49),
        K_ADDCOHORT_AFTER (50),
        K_ADDCOHORT_BEFORE (51),
        K_EXTERNAL (52),
        K_EXTERNAL_ONCE (53),
        K_EXTERNAL_ALWAYS (54),
        KEYWORD_COUNT (55);
        public final int value;
        KEYWORDS(int v) {
            this.value = v;
        }
    }
    
    public enum STRINGS {
        S_IGNORE (0),
        S_PIPE (1),
        S_TO (2),
        S_OR (3),
        S_PLUS (4),
        S_MINUS (5),
        S_MULTIPLY (6),
        S_ASTERIKTWO (7),
        S_FAILFAST (8),
        S_BACKSLASH (9),
        S_HASH (10),
        S_NOT (11),
        S_TEXTNOT (12),
        S_TEXTNEGATE (13),
        S_ALL (14),
        S_NONE (15),
        S_LINK (16),
        S_BARRIER (17),
        S_CBARRIER (18),
        S_CMD_FLUSH (19),
        S_CMD_EXIT (20),
        S_CMD_IGNORE (21),
        S_CMD_RESUME (22),
        S_TARGET (23),
        S_AND (24),
        S_IF (25),
        S_DELIMITSET (26),
        S_SOFTDELIMITSET (27),
        S_BEGINTAG (28),
        S_ENDTAG (29),
        S_LINKZ (30),
        S_SPACE (31),
        S_UU_LEFT (32),
        S_UU_RIGHT (33),
        S_UU_PAREN (34),
        S_UU_TARGET (35),
        S_UU_MARK (36),
        S_UU_ATTACHTO (37),
        S_RXTEXT_ANY (38),
        S_RXBASE_ANY (39),
        S_RXWORD_ANY (40),
        S_AFTER (41),
        S_BEFORE (42),
        S_WITH (43),
        S_QUESTION (44),
        S_VS1 (45),
        S_VS2 (46),
        S_VS3 (47),
        S_VS4 (48),
        S_VS5 (49),
        S_VS6 (50),
        S_VS7 (51),
        S_VS8 (52),
        S_VS9 (53),
        S_VSu (54),
        S_VSU (55),
        S_VSl (56),
        S_VSL (57),
        S_GPREFIX (58),
        S_POSITIVE (59),
        S_NEGATIVE (60),
        S_ONCE (61),
        S_ALWAYS (62),
        S_SET_ISECT_U (63),
        S_SET_SYMDIFF_U (64),
        S_FROM (65),
        STRINGS_COUNT (66);
        public final int value;
        STRINGS(int v) {
            this.value = v;
        }
    }
    
    // this should be kept in lock-step with Rule's RF enum
    public enum SFLAGS {
        FL_NEAREST       (0),
        FL_ALLOWLOOP     (1),
        FL_DELAYED       (2),
        FL_IMMEDIATE     (3),
        FL_LOOKDELETED   (4),
        FL_LOOKDELAYED   (5),
        FL_UNSAFE        (6),
        FL_SAFE          (7),
        FL_REMEMBERX     (8),
        FL_RESETX        (9),
        FL_KEEPORDER     (10),
        FL_VARYORDER     (11),
        FL_ENCL_INNER    (12),
        FL_ENCL_OUTER    (13),
        FL_ENCL_FINAL    (14),
        FL_ENCL_ANY      (15),
        FL_ALLOWCROSS    (16),
        FL_WITHCHILD     (17),
        FL_NOCHILD       (18),
        FL_ITERATE       (19),
        FL_NOITERATE     (20),
        FL_UNMAPLAST     (21),
        FL_REVERSE       (22),
        FLAGS_COUNT      (23);
        public int value;
        SFLAGS(int v) {
            this.value = v;
        }
    }
    
    // need to write some real hashing functions
    public static final int CG3_HASH_SEED = 705577479;
    // this isn't really a good way to do this, for a number of reasons, but I really want this code to run
    public static int hash_sdbm_char(String str, int hash, int length) {
        if (hash == 0) {
            hash = CG3_HASH_SEED;
        }
        if (length == 0) {
            length = str.length();
        }
        
        return (int)RSHash(str);
    }
    
    public static int hash_sdbm_uchar(String str, int hash, int length) {
        if (hash == 0) {
            hash = CG3_HASH_SEED;
        }
        if (length == 0) {
            length = str.length();
        }
        return (int) JSHash(str);
    }
    
    public static int hash_sdbm_uint32_t(int c, int hash) {
        if (hash == 0) {
            hash = CG3_HASH_SEED;
        }
        hash = c + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    /*
    public static int SuperFastHash_char(String data, int hash, int len) {
        int tmp;
        int rem;
        if (len == 0 || data == null) {
            return 0;
        }
        rem = len & 3;
        len >>= 2;
        // Main loop
        for (;len > 0;len--) {
            hash += get16bits(data);
            tmp = (get16bits(data.substring(2)) << 11) ^ hash;
            hash = (hash << 16) ^ tmp;
            
        }
    }
    
    public static int get16bits(String d) {
        int a = (d.getBytes()[1] << 8) / (d.getBytes()[0]);
        return a;
    }
    */
    /*
     **************************************************************************
     *                                                                        *
     *          General Purpose Hash Function Algorithms Library              *
     *                                                                        *
     * Author: Arash Partow - 2002                                            *
     * URL: http://www.partow.net                                             *
     * URL: http://www.partow.net/programming/hashfunctions/index.html        *
     *                                                                        *
     * Copyright notice:                                                      *
     * Free use of the General Purpose Hash Function Algorithms Library is    *
     * permitted under the guidelines and in accordance with the most current *
     * version of the Common Public License.                                  *
     * http://www.opensource.org/licenses/cpl1.0.php                          *
     *                                                                        *
     **************************************************************************
    */




       public static long RSHash(String str)
       {
          int b     = 378551;
          int a     = 63689;
          long hash = 0;

          for(int i = 0; i < str.length(); i++)
          {
             hash = hash * a + str.charAt(i);
             a    = a * b;
          }

          return hash;
       }
       /* End Of RS Hash Function */


       public static long JSHash(String str)
       {
          long hash = 1315423911;

          for(int i = 0; i < str.length(); i++)
          {
             hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
          }

          return hash;
       }
       /* End Of JS Hash Function */


       public long PJWHash(String str)
       {
          long BitsInUnsignedInt = (long)(4 * 8);
          long ThreeQuarters     = (long)((BitsInUnsignedInt  * 3) / 4);
          long OneEighth         = (long)(BitsInUnsignedInt / 8);
          long HighBits          = (long)(0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
          long hash              = 0;
          long test              = 0;

          for(int i = 0; i < str.length(); i++)
          {
             hash = (hash << OneEighth) + str.charAt(i);

             if((test = hash & HighBits)  != 0)
             {
                hash = (( hash ^ (test >> ThreeQuarters)) & (~HighBits));
             }
          }

          return hash;
       }
       /* End Of  P. J. Weinberger Hash Function */


       public long ELFHash(String str)
       {
          long hash = 0;
          long x    = 0;

          for(int i = 0; i < str.length(); i++)
          {
             hash = (hash << 4) + str.charAt(i);

             if((x = hash & 0xF0000000L) != 0)
             {
                hash ^= (x >> 24);
             }
             hash &= ~x;
          }

          return hash;
       }
       /* End Of ELF Hash Function */


       public long BKDRHash(String str)
       {
          long seed = 131; // 31 131 1313 13131 131313 etc..
          long hash = 0;

          for(int i = 0; i < str.length(); i++)
          {
             hash = (hash * seed) + str.charAt(i);
          }

          return hash;
       }
       /* End Of BKDR Hash Function */


       public long SDBMHash(String str)
       {
          long hash = 0;

          for(int i = 0; i < str.length(); i++)
          {
             hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
          }

          return hash;
       }
       /* End Of SDBM Hash Function */


       public long DJBHash(String str)
       {
          long hash = 5381;

          for(int i = 0; i < str.length(); i++)
          {
             hash = ((hash << 5) + hash) + str.charAt(i);
          }

          return hash;
       }
       /* End Of DJB Hash Function */


       public long DEKHash(String str)
       {
          long hash = str.length();

          for(int i = 0; i < str.length(); i++)
          {
             hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
          }

          return hash;
       }
       /* End Of DEK Hash Function */


       public long BPHash(String str)
       {
          long hash = 0;

          for(int i = 0; i < str.length(); i++)
          {
             hash = hash << 7 ^ str.charAt(i);
          }

          return hash;
       }
       /* End Of BP Hash Function */


       public long FNVHash(String str)
       {
          long fnv_prime = 0x811C9DC5;
          long hash = 0;

          for(int i = 0; i < str.length(); i++)
          {
          hash *= fnv_prime;
          hash ^= str.charAt(i);
          }

          return hash;
       }
       /* End Of FNV Hash Function */


       public long APHash(String str)
       {
          long hash = 0xAAAAAAAA;

          for(int i = 0; i < str.length(); i++)
          {
             if ((i & 1) == 0)
             {
                hash ^= ((hash << 7) ^ str.charAt(i) * (hash >> 3));
             }
             else
             {
                hash ^= (~((hash << 11) + str.charAt(i) ^ (hash >> 5)));
             }
          }

          return hash;
       }
       /* End Of AP Hash Function */

    }
    
    
    
    
    
    
    
    

