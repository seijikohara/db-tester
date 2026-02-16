package example.feature

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Deeply composed annotation combining {@link UserSeedData} and
 * {@link VerifyIgnoringAuditColumns}.
 *
 * <p>Demonstrates two-level meta-annotation traversal: the framework discovers {@code @DataSet}
 * through {@code @UserDataTest} then {@code @UserSeedData} then {@code @DataSet}, and
 * {@code @ExpectedDataSet} through {@code @UserDataTest} then {@code @VerifyIgnoringAuditColumns}
 * then {@code @ExpectedDataSet}.
 */
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE])
@Retention(RetentionPolicy.RUNTIME)
@UserSeedData
@VerifyIgnoringAuditColumns
@interface UserDataTest {}
