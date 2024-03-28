import React, { useState } from 'react';

import './exporter.module.scss';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle, IconButton, Link,
  MenuItem,
  TextField,
  Typography
} from '@material-ui/core';
import { ExportTarget } from '../../../../../gen/api-client';
import { useDeleteExportedProject, useExportedProjects, useExportProject } from '../../../project/data/project.hooks';
import colors from '../../../../../assets/styling/colors.json';
import ExpandMore from '@material-ui/icons/ExpandMore';
import ExpandLess from '@material-ui/icons/ExpandLess';

export interface ExporterProps {
  visible: boolean;
  projectUuid: string;
  onClose: () => void;
}

export function Exporter(props: ExporterProps) {
  const [targetPlatform, setTargetPlatform] = useState<ExportTarget>(ExportTarget.ReactTypescript);
  const [errorsVisible, setErrorsVisible] = useState<boolean>(false);
  const {isLoading, isError, exportedProjectDtos, error} = useExportedProjects();
  const exportProject = useExportProject(props.projectUuid);
  const deleteExportedProject = useDeleteExportedProject(props.projectUuid);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error}</div>;
  }

  const exportedProjectDto = exportedProjectDtos.find(
    exportedProjectDto => exportedProjectDto.uuid === props.projectUuid
  );

  const handleChangeTargetPlatform = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTargetPlatform(event.target.value as ExportTarget);
  }

  const handleExportProject = () => {
    exportProject.mutate({ exportTarget: targetPlatform });
    setErrorsVisible(false);
  };

  const handleDeleteClick = () => deleteExportedProject.mutate();

  const handleErrorsVisibleClick = () => setErrorsVisible(!errorsVisible);

  const getExportTargetName = (exportTarget: ExportTarget) => {
    switch (exportTarget) {
      case ExportTarget.ReactTypescript:
        return 'React/TypeScript';
      default:
        return 'unknown';
    }
  }

  // https://stackoverflow.com/a/67068903 [accessed 23/9/2023, 16:01]
  return (!props.visible) ? null : (
    <Dialog
      PaperProps={{style: {borderRadius: 20, padding: 20}}}
      fullWidth={true}
      maxWidth={'sm'}
      open={props.visible}
      onClose={props.onClose}
    >
      <DialogTitle style={{paddingBottom: 0}}>Export Project</DialogTitle>
      <DialogContent style={{paddingTop: 0}}>
        <Typography variant={'body1'} color={'textSecondary'}>Export and download platform specific applications</Typography>
        <TextField
          style={{marginTop: 25}}
          variant={'outlined'}
          size={'small'}
          fullWidth={true}
          select={true}
          label={'Target Platform'}
          value={targetPlatform}
          onChange={handleChangeTargetPlatform}
        >
          <MenuItem value={ExportTarget.ReactTypescript}>{getExportTargetName(ExportTarget.ReactTypescript)}</MenuItem>
        </TextField>
        {(exportedProjectDto !== undefined)
          ? <Box
              style={{
                borderRadius: 10,
                marginTop: 20,
                marginBottom: 5,
                padding: 20
              }}
              boxShadow={1}
          >
            <Typography variant={'body2'} color={'secondary'}>Download available</Typography>
            <div style={{marginTop: 10}}>
              <Typography variant={'body1'} color={'textPrimary'}>The project was exported successfully and is now available for download. Further exports will overwrite this one!</Typography>
            </div>
            <div style={{ marginTop: 5 }}>
              <Typography variant={'body2'} color={'textSecondary'}>Platform: {getExportTargetName(exportedProjectDto.exportTarget)}</Typography>
              {(exportedProjectDto.errors.length !== 0)
                ? <div>
                  <div style={{display: 'flex', alignItems: 'center'}}>
                    <Typography variant={'body2'} color={'textSecondary'}>The exported project contains errors!</Typography>
                    <IconButton size={'small'} onClick={handleErrorsVisibleClick}>
                      {(errorsVisible) ? <ExpandLess fontSize={'small'}/> : <ExpandMore fontSize={'small'}/>}
                    </IconButton>
                  </div>
                    {(errorsVisible)
                      ? <div style={{
                          backgroundColor: colors.background_light,
                          borderRadius: 10,
                          boxSizing: 'border-box',
                          maxHeight: 150,
                          overflowY: 'auto',
                          padding: 10
                      }}>
                        {exportedProjectDto.errors.map(
                          (error, index) => <Typography
                            key={index}
                            display={'block'}
                            variant={'caption'}
                            color={'textPrimary'}
                          >{error.message}</Typography>
                        )}
                      </div>
                      : null}
                </div>
                : null}
              <div style={{display: 'flex', alignItems: 'center', marginTop: 10}}>
                <Link
                  style={{marginRight: 10}}
                  href={`http://localhost:3333/api/project/${props.projectUuid}/download`}
                >
                  <Button variant={'contained'} color={'secondary'}>Download</Button>
                </Link>
                <Button variant={'contained'} color={'primary'} onClick={handleDeleteClick}>Delete</Button>
              </div>
            </div>
          </Box>
          : null
        }
      </DialogContent>
      <DialogActions>
        <Button onClick={props.onClose}>Close</Button>
        <Button onClick={handleExportProject}>Export Project</Button>
      </DialogActions>
    </Dialog>
  );
}

export default Exporter;
