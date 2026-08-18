create table cq_account (
    id uuid primary key,
    github_user_id bigint not null unique,
    github_login varchar(255) not null,
    display_name varchar(255),
    avatar_url varchar(2048),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint cq_account_github_login_not_blank check (length(trim(github_login)) > 0),
    constraint cq_account_timestamps_ordered check (updated_at >= created_at)
);

create table cq_oauth_state (
    id uuid primary key,
    account_id uuid references cq_account (id) on delete cascade,
    state_digest char(64) not null unique,
    return_path varchar(512) not null,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    constraint cq_oauth_state_relative_return check (return_path like '/%' and return_path not like '//%'),
    constraint cq_oauth_state_expiry_ordered check (expires_at > created_at),
    constraint cq_oauth_state_consumed_ordered check (consumed_at is null or consumed_at >= created_at)
);

create index cq_oauth_state_account_idx on cq_oauth_state (account_id) where account_id is not null;
create index cq_oauth_state_expiry_idx on cq_oauth_state (expires_at);

create table cq_user_session (
    id uuid primary key,
    account_id uuid not null references cq_account (id) on delete cascade,
    token_digest char(64) not null unique,
    csrf_digest char(64) not null,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    constraint cq_user_session_expiry_ordered check (expires_at > created_at),
    constraint cq_user_session_revoked_ordered check (revoked_at is null or revoked_at >= created_at)
);

create index cq_user_session_account_idx on cq_user_session (account_id);
create index cq_user_session_expiry_idx on cq_user_session (expires_at) where revoked_at is null;

create table cq_saved_campaign (
    id uuid primary key,
    account_id uuid not null references cq_account (id) on delete cascade,
    repository_owner varchar(100) not null,
    repository_name varchar(100) not null,
    projection jsonb not null,
    visibility varchar(16) not null,
    projection_schema_version integer not null,
    mapping_algorithm_version integer not null,
    scoring_ruleset_version integer not null,
    export_schema_version integer not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint cq_saved_campaign_owner_normalized check (repository_owner = lower(repository_owner)),
    constraint cq_saved_campaign_name_normalized check (repository_name = lower(repository_name)),
    constraint cq_saved_campaign_owner_not_blank check (length(trim(repository_owner)) > 0),
    constraint cq_saved_campaign_name_not_blank check (length(trim(repository_name)) > 0),
    constraint cq_saved_campaign_visibility check (visibility in ('private', 'unlisted')),
    constraint cq_saved_campaign_versions_positive check (
        projection_schema_version > 0
        and mapping_algorithm_version > 0
        and scoring_ruleset_version > 0
        and export_schema_version > 0
    ),
    constraint cq_saved_campaign_timestamps_ordered check (updated_at >= created_at),
    unique (account_id, repository_owner, repository_name)
);

create index cq_saved_campaign_owner_updated_idx
    on cq_saved_campaign (account_id, updated_at desc, id);
