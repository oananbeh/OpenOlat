-- Business group 
alter table o_gp_business add column status varchar(32) default 'active';

alter table o_gp_business add column inactivationdate timestamp;
alter table o_gp_business add column inactivationemaildate timestamp;
alter table o_gp_business add column reactivationdate timestamp;

alter table o_gp_business add column softdeleteemaildate timestamp;
alter table o_gp_business add column softdeletedate timestamp;

alter table o_gp_business add column data_for_restore text;

alter table o_gp_business add column fk_inactivatedby_id int8;
alter table o_gp_business add column fk_softdeletedby_id int8;

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);
create index idx_gb_bus_inactivateby_idx on o_gp_business (fk_inactivatedby_id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);
create index idx_gb_bus_softdeletedby_idx on o_gp_business (fk_softdeletedby_id);


-- Course
create table o_course_element (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_type varchar(32) not null,
   c_short_title varchar(32) not null,
   c_long_title varchar(1024) not null,
   c_assesseable bool not null,
   c_score_mode varchar(16) not null,
   c_passed_mode varchar(16) not null,
   c_cut_value decimal,
   fk_entry int8 not null,
   c_subident varchar(64) not null,
   primary key (id)
);

alter table o_course_element add constraint courseele_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_courseele_entry_idx on o_course_element (fk_entry);
create unique index idx_courseele_subident_idx on o_course_element (c_subident, fk_entry);


-- Assessment
alter table o_as_entry add a_obligation_inherited varchar(50);
alter table o_as_entry add a_obligation_evaluated varchar(50);
alter table o_as_entry add a_obligation_config varchar(50);
alter table o_as_entry add a_max_score decimal;
alter table o_as_entry add fk_identity_status_done int8;
create index idx_as_entry_to_courseele_idx on o_as_entry (fk_entry, a_subident);
create index idx_as_entry_inreview_idx on o_as_entry (a_status) where a_status = 'inReview';

create table o_as_score_accounting_trigger (
   id bigserial,
   creationdate timestamp not null,
   e_identifier varchar(64) not null,
   e_business_group_key int8,
   e_organisation_key int8,
   e_curriculum_element_key int8,
   e_user_property_name varchar(64),
   e_user_property_value varchar(128),
   fk_entry int8 not null,
   e_subident varchar(64) not null,
   primary key (id)
);

alter table o_as_score_accounting_trigger add constraint satrigger_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_satrigger_re_idx on o_as_score_accounting_trigger (fk_entry);
create index idx_satrigger_bs_group_idx on o_as_score_accounting_trigger (e_business_group_key) where e_business_group_key is not null;
create index idx_satrigger_org_idx on o_as_score_accounting_trigger (e_organisation_key) where e_organisation_key is not null;
create index idx_satrigger_curle_idx on o_as_score_accounting_trigger (e_curriculum_element_key) where e_curriculum_element_key is not null;
create index idx_satrigger_userprop_idx on o_as_score_accounting_trigger (e_user_property_value, e_user_property_name) where e_user_property_value is not null;


-- Taxonomy Types --
alter table o_tax_taxonomy_level_type add t_allow_as_subject boolean default false;
