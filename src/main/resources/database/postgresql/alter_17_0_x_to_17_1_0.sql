
-- Catalog
create table o_ca_launcher_to_organisation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_launcher int8 not null,
   fk_organisation int8 not null,
   primary key (id)
);

alter table o_ca_launcher_to_organisation add constraint rel_lto_launcher_idx foreign key (fk_launcher) references o_ca_launcher(id);
create index idx_rel_lto_launcher_idx on o_ca_launcher_to_organisation (fk_launcher);
alter table o_ca_launcher_to_organisation add constraint rel_lto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_lto_org_idx on o_ca_launcher_to_organisation (fk_organisation);


-- External user
alter table o_bs_invitation add column i_status varchar(32) default 'active';


-- Business group
alter table o_gp_business add column excludeautolifecycle bool default false not null;


-- Task
alter table o_gta_task add column g_submission_drole varchar(16);
alter table o_gta_task add column g_submission_revisions_drole varchar(16);
alter table o_gta_task add column g_collection_revisions_date timestamp;
alter table o_gta_task add column g_collection_revisions_ndocs int8;

-- VFS metadata
alter table o_vfs_metadata add column f_transcoding_status int8;
create index idx_vfs_meta_transstat_idx on o_vfs_metadata(f_transcoding_status);
