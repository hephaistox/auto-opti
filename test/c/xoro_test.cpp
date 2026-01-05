#include <stdint.h>
#include <stdio.h>
   
static inline uint64_t rotl(const uint64_t x, int k) {
   return (x << k) | (x >> (64 - k));
}
   
static uint64_t s[2];
   
uint64_t next(void) {
   const uint64_t s0 = s[0];
   uint64_t s1 = s[1];
   const uint64_t result = s0 + s1;
       
   s1 ^= s0;
   s[0] = rotl(s0, 24) ^ s1 ^ (s1 << 16);
   s[1] = rotl(s1, 37);
       
   return result;
}
   
int main(void) {
   // Initialize with known state
   s[0] = 1;
   s[1] = 2;
       
   for(int i = 0; i < 20; i++) {
       printf("%llu\n", next());
   }
   return 0;
}
