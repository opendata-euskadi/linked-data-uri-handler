package r01hp.lod.config;

import com.google.common.base.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.Role;
import r01f.types.url.Host;
import r01f.util.types.Strings;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HLODTripleStoreHostWithRole
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final Host _host;
	@Getter private final Role _role;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01HLODTripleStoreHostWithRole create(final Host host,final Role role) {
		return new R01HLODTripleStoreHostWithRole(host,role);
	}
	public static R01HLODTripleStoreHostWithRole creatWithReadRole(final Host host) {
		return new R01HLODTripleStoreHostWithRole(host,READ_ROLE);	
	}
	public static R01HLODTripleStoreHostWithRole createWithWriteRole(final Host host) {
		return new R01HLODTripleStoreHostWithRole(host,WRITE_ROLE);
	}
	public static R01HLODTripleStoreHostWithRole createWithReadAndWriteRole(final Host host) {
		return new R01HLODTripleStoreHostWithRole(host,READ_WRITE_ROLE);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	public boolean hasReadRole() {
		return R01HLODTripleStoreHostWithRole.hasReadRole(_role);
	}
	public boolean hasWriteRole() {
		return R01HLODTripleStoreHostWithRole.hasWriteRole(_role);
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("{} ({})",
								  _host,_role);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static Role READ_ROLE = Role.forId("read");
	public static Role WRITE_ROLE = Role.forId("write");
	public static Role READ_WRITE_ROLE = Role.forId("read/write");
	
	public static boolean hasReadRole(final Role role) {
		return role != null 
			&& role.isContainedIn(READ_ROLE,
								  READ_WRITE_ROLE);		
	}
	public static boolean hasWriteRole(final Role role) {		
		return role != null
			&& role.isContainedIn(WRITE_ROLE,
								  READ_WRITE_ROLE);
	}
	
	public static Predicate<R01HLODTripleStoreHostWithRole> matcherFor(final Role role) {
		return new Predicate<R01HLODTripleStoreHostWithRole>() {
						@Override
						public boolean apply(final R01HLODTripleStoreHostWithRole host) {
							return role != null ? role.is(host.getRole())
												: true;	// if the given role is null matches
						}
			   };
	}
}
