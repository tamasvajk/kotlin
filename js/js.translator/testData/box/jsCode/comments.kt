// EXPECTED_REACHABLE_NODES: 1282
// CHECK_COMMENT_EXISTS: text="Single line comment" multiline=false
// CHECK_COMMENT_EXISTS: text="Multi line comment" multiline=true
// CHECK_COMMENT_EXISTS: text="Single line comment inside function" multiline=false
// CHECK_COMMENT_EXISTS: text="Multi line comment inside function" multiline=true
// CHECK_COMMENT_EXISTS: text="After call single line comment" multiline=false
// CHECK_COMMENT_EXISTS: text="After call multi line comment" multiline=true
// CHECK_COMMENT_EXISTS: text="Multi line comment inside function" multiline=true
// CHECK_COMMENT_EXISTS: text="After call single line comment" multiline=false
// CHECK_COMMENT_EXISTS: text="After call multi line comment" multiline=true
// CHECK_COMMENT_EXISTS: text="Before argument 1" multiline=true
// CHECK_COMMENT_EXISTS: text="Before argument 2" multiline=true
// CHECK_COMMENT_EXISTS: text="After argument 1" multiline=true
// CHECK_COMMENT_EXISTS: text="After argument 2" multiline=true
// CHECK_COMMENT_EXISTS: text="object:" multiline=true
// CHECK_COMMENT_EXISTS: text="property:" multiline=true
// CHECK_COMMENT_EXISTS: text="descriptor:" multiline=true
// CHECK_COMMENT_EXISTS: text="Descriptor end" multiline=true

package foo

fun box(): String {
    js("""
        function foo() {
            // Single line comment inside function
            Object;
            /*Multi line comment inside function*/
        }
        
        // Single line comment
        foo();
        
        /* Multi line comment */
        foo();
        
        foo(); // After call single line comment
        
        foo(); /* After call multi line comment */
        
        foo(
            /* Before argument 1 */
            /* Before argument 2 */
            4
            /* After argument 1 */
            /* After argument 2 */
        );
        
        var test = {
             test: Object.defineProperty(/* object: */{}, /* property: */'some_property', /* descriptor: */ {
              value: 42,
              writable: false
            } /* Descriptor end */)
        }
    """)
    return "OK"
}